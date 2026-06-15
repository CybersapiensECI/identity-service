package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.VerificationPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationUseCase implements VerificationPort {

    private static final long OTP_DURATION_MILLIS = 10 * 60 * 1000L; // 10 minutes — matches Redis TTL
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepositoryPort userRepository;
    private final OtpRepositoryPort otpRepository;
    private final EmailSenderPort emailSender;

    @Override
    public void initVerification(String email) {
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
}
