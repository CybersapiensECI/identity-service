package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidCredentialsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpInvalidException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpMaxAttemptsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.PasswordPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.PasswordEncoderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpCode;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.PasswordHash;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordUseCase implements PasswordPort {

    private static final long OTP_DURATION_MILLIS = 15 * 60 * 1000L; // 15 minutes
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final OtpRepositoryPort otpRepository;
    private final EmailSenderPort emailSender;

    @Override
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidInputException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword().getValue())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        new PasswordHash(newPassword); // validates strength before encoding
        user.setPassword(PasswordHash.fromEncoded(passwordEncoder.encode(newPassword)));
        userRepository.update(user);
    }

    @Override
    public void forgotPassword(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidInputException("User with given email not found"));

        String rawCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        OtpEmbedded resetOtp = new OtpEmbedded(rawCode, OTP_DURATION_MILLIS);

        otpRepository.save(normalizedEmail, resetOtp, OtpType.PASSWORD_RESET);
        emailSender.sendPasswordReset(normalizedEmail, rawCode);
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidInputException("User with given email not found"));

        OtpEmbedded otp = otpRepository.findByEmail(normalizedEmail, OtpType.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidInputException("No password reset request found for this email"));

        if (!otp.isValid()) {
            throw new InvalidInputException("OTP code has expired or was already used");
        }

        if (!otp.getCode().equals(OtpCode.of(code))) {
            otp.incrementAttempts();
            if (otp.hasReachedLimit()) {
                otpRepository.delete(normalizedEmail, OtpType.PASSWORD_RESET);
                throw new OtpMaxAttemptsException("Maximum attempts reached, please request a new code");
            }
            otpRepository.save(normalizedEmail, otp, OtpType.PASSWORD_RESET);
            throw new OtpInvalidException("OTP code is incorrect");
        }
        new PasswordHash(newPassword);
        
        otpRepository.delete(normalizedEmail, OtpType.PASSWORD_RESET);

        user.setPassword(PasswordHash.fromEncoded(passwordEncoder.encode(newPassword)));
        userRepository.update(user);
    }
}
