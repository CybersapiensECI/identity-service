package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.response.LoginResponseDto;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.AccountBlocked;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.EmailNotVerifiedException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidCredentialsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.TokenExpiredException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.TokenInvalidException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.RefreshToken;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.LoginPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.JwtProviderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.LockoutRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.PasswordEncoderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.RefreshTokenRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginUseCase implements LoginPort {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int REFRESH_TOKEN_DAYS = 7;

    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final LockoutRepositoryPort lockoutRepository;
    private final JwtProviderPort jwtProvider;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    public LoginResponseDto login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        int failedAttempts = lockoutRepository.findFailedAttempts(normalizedEmail);
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            throw new AccountBlocked("Account temporarily locked. Please try again in 30 minutes.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            lockoutRepository.incrementFailedAttempts(normalizedEmail);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isVerified()) {
            throw new EmailNotVerifiedException("Email not verified. Check your inbox for the OTP.");
        }

        if (user.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlocked("Your account has been blocked. Please contact support.");
        }

        lockoutRepository.clearFailedAttempts(normalizedEmail);

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshTokenValue = jwtProvider.generateRefreshToken(user);

        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken session = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .token(refreshTokenValue)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .build();
        session = refreshTokenRepository.save(session);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(session.getToken())
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenInvalidException("Refresh token not found"));

        refreshTokenRepository.revoke(refreshToken);
    }

    @Override
    public LoginResponseDto refresh(String refreshToken) {
        RefreshToken session = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenInvalidException("Refresh token not found"));

        if (session.isRevoked() || session.isExpired()) {
            throw new TokenExpiredException("Refresh token has expired or been revoked");
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("User associated with token not found"));

        if (user.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlocked("Your account has been blocked. Please contact support.");
        }

        refreshTokenRepository.revoke(refreshToken);

        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshTokenValue = jwtProvider.generateRefreshToken(user);

        RefreshToken newSession = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .token(newRefreshTokenValue)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .build();

        newSession = refreshTokenRepository.save(newSession);

        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newSession.getToken())
                .tokenType("Bearer")
                .build();
    }
}
