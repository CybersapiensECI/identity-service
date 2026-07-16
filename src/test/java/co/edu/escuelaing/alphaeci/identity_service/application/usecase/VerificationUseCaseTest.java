package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.EmailNotVerifiedException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.UserAlreadyExistsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.RegistrationProfile;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.Role;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EventPublisherPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.PasswordEncoderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.validation.PasswordValidator;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;

@ExtendWith(MockitoExtension.class)
class VerificationUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private OtpRepositoryPort otpRepository;
    @Mock private EmailSenderPort emailSender;
    @Mock private PasswordEncoderPort passwordEncoder;
    @Mock private EventPublisherPort eventPublisher;
    @Mock private PasswordValidator passwordValidator;

    @InjectMocks private VerificationUseCase verificationUseCase;

    private static final String EMAIL = "new@escuelaing.edu.co";
    private static final String VALID_PASSWORD = "SecurePass123!";

    private User pendingUser() {
        return User.builder()
                .id("uid-new")
                .email(new Email(EMAIL))
                .password("$2a$10$hash")
                .role(Role.STUDENT)
                .status(AccountStatus.PENDING_VERIFICATION)
                .build();
    }

    private User verifiedUser() {
        User user = pendingUser();
        user.setVerified(true);
        user.setStatus(AccountStatus.ACTIVE);
        return user;
    }

    private RegistrationProfile sampleProfile() {
        return new RegistrationProfile(
                "Juan Pérez", "MALE", "SYSTEMS_ENGINEERING", 5,
                "2019050123", "https://photo.url/img.jpg", "Bio text",
                "PUBLIC", LocalDate.of(2001, 5, 15), true);
    }

    // ── initVerification ───────────────────────────────────────────────────────

    @Test
    void initVerification_success_savesUserOtpAndSendsEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$encoded");

        verificationUseCase.initVerification(EMAIL, VALID_PASSWORD);

        verify(userRepository).save(any(User.class));
        verify(otpRepository).save(eq(EMAIL), any(OtpEmbedded.class), eq(OtpType.EMAIL_VERIFICATION));
        verify(emailSender).sendOtp(anyString(), eq(EMAIL), anyString());
    }

    @Test
    void initVerification_emailAlreadyExists_throwsUserAlreadyExists() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(pendingUser()));

        assertThatThrownBy(() -> verificationUseCase.initVerification(EMAIL, VALID_PASSWORD))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void initVerification_weakPassword_throwsInvalidInput() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        doThrow(new InvalidInputException("Weak password"))
            .when(passwordValidator)
            .isValid("weak");

        assertThatThrownBy(() -> verificationUseCase.initVerification(EMAIL, "weak"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void initVerification_normalizesEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$encoded");

        verificationUseCase.initVerification("  NEW@ESCUELAING.EDU.CO  ", VALID_PASSWORD);

        verify(userRepository).findByEmail(EMAIL);
    }

    // ── completeRegistration ───────────────────────────────────────────────────

    @Test
    void completeRegistration_userNotFound_throwsInvalidInput() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationUseCase.completeRegistration(EMAIL, sampleProfile()))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void completeRegistration_emailNotVerified_throwsEmailNotVerifiedException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(pendingUser()));

        assertThatThrownBy(() -> verificationUseCase.completeRegistration(EMAIL, sampleProfile()))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void completeRegistration_success_publishesEvent() {
        User user = verifiedUser();
        RegistrationProfile profile = sampleProfile();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        verificationUseCase.completeRegistration(EMAIL, profile);

        verify(eventPublisher).publishUserVerified(user, profile);
    }

    @Test
    void completeRegistration_normalizesEmail() {
        User user = verifiedUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        verificationUseCase.completeRegistration("  NEW@ESCUELAING.EDU.CO  ", sampleProfile());

        verify(userRepository).findByEmail(EMAIL);
    }
}
