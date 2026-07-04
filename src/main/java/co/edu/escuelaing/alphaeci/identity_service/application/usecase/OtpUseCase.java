package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.response.LoginResponseDto;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpExpiredException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpInvalidException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpMaxAttemptsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.RefreshToken;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.OtpPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.JwtProviderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.RefreshTokenRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpCode;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpUseCase implements OtpPort {

    private static final long OTP_DURATION_MILLIS = 10 * 60 * 1000L; // 10 minutes — matches Redis TTL
    private static final int REFRESH_TOKEN_DAYS = 7;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepositoryPort userRepository;
    private final OtpRepositoryPort otpRepository;
    private final EmailSenderPort emailSender;
    private final JwtProviderPort jwtProvider;
    private final RefreshTokenRepositoryPort refreshTokenRepository;

    @Override
    public void resendOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidInputException("No account found for this email"));

        if (user.isVerified()) {
            throw new InvalidInputException("Email is already verified");
        }

        String rawCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        OtpEmbedded otp = new OtpEmbedded(rawCode, OTP_DURATION_MILLIS);

        otpRepository.save(normalizedEmail, otp, OtpType.EMAIL_VERIFICATION);
        emailSender.sendOtp(normalizedEmail, rawCode);
    }

    @Override
    public LoginResponseDto validateOtp(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase();

        new OtpCode(code);

        OtpEmbedded otp = otpRepository.findByEmail(normalizedEmail, OtpType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new OtpExpiredException("OTP has expired. Please request a new one"));

        if (!otp.isValid()) {
            throw new OtpExpiredException("OTP has expired or was already used");
        }

        if (!otp.getCode().equals(OtpCode.of(code))) {
            otp.incrementAttempts();
            if (otp.hasReachedLimit()) {
                otpRepository.delete(normalizedEmail, OtpType.EMAIL_VERIFICATION);
                throw new OtpMaxAttemptsException("Maximum OTP attempts reached. Please request a new code");
            }
            otpRepository.save(normalizedEmail, otp, OtpType.EMAIL_VERIFICATION);
            throw new OtpInvalidException("Invalid OTP");
        }

        otpRepository.delete(normalizedEmail, OtpType.EMAIL_VERIFICATION);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new OtpInvalidException("User not found"));

        user.setVerified(true);
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.update(user);

        emailSender.sendVerificationSuccess(normalizedEmail);

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
}
