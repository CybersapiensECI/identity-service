package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.EmailNotVerifiedException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.UserAlreadyExistsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.RegistrationProfile;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.Role;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.VerificationPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EventPublisherPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.PasswordEncoderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.PasswordHash;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationUseCase implements VerificationPort {

    private static final long OTP_DURATION_MILLIS = 10 * 60 * 1000L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepositoryPort userRepository;
    private final OtpRepositoryPort otpRepository;
    private final EmailSenderPort emailSender;
    private final PasswordEncoderPort passwordEncoder;
    private final EventPublisherPort eventPublisher;

    @Override
    public void initVerification(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException("An account with this email already exists");
        }

        new PasswordHash(password);

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(new Email(normalizedEmail));
        user.setPassword(PasswordHash.fromEncoded(encodedPassword));
        user.setRole(Role.STUDENT);
        user.setStatus(AccountStatus.PENDING_VERIFICATION);
        user.setVerified(false);
        user.setFailedAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String rawCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        OtpEmbedded otp = new OtpEmbedded(rawCode, OTP_DURATION_MILLIS);
        otpRepository.save(normalizedEmail, otp, OtpType.EMAIL_VERIFICATION);
        emailSender.sendOtp(normalizedEmail, rawCode);
    }

    @Override
    public void completeRegistration(String email, RegistrationProfile profile) {
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidInputException("No account found for this email"));

        if (!user.isVerified()) {
            throw new EmailNotVerifiedException("Email must be verified before completing registration");
        }

        eventPublisher.publishUserVerified(user, profile);
    }
}
