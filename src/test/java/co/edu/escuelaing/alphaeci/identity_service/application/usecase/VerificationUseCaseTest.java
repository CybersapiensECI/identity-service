package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.Role;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.PasswordHash;

@ExtendWith(MockitoExtension.class)
class VerificationUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private OtpRepositoryPort otpRepository;
    @Mock private EmailSenderPort emailSender;

    @InjectMocks private VerificationUseCase verificationUseCase;

    private static final String EMAIL = "new@escuelaing.edu.co";

    private User unverifiedUser() {
        User user = new User();
        user.setId("uid-new");
        user.setEmail(new Email(EMAIL));
        user.setPassword(PasswordHash.fromEncoded("$2a$10$hash"));
        user.setRole(Role.STUDENT);
        user.setStatus(AccountStatus.PENDING_VERIFICATION);
        user.setVerified(false);
        return user;
    }

    @Test
    void initVerification_success_savesOtpAndSendsEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(unverifiedUser()));

        verificationUseCase.initVerification(EMAIL);

        verify(otpRepository).save(eq(EMAIL), any(OtpEmbedded.class), eq(OtpType.EMAIL_VERIFICATION));
        verify(emailSender).sendOtp(eq(EMAIL), anyString());
    }

    @Test
    void initVerification_userNotFound_throwsInvalidInput() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationUseCase.initVerification(EMAIL))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void initVerification_alreadyVerified_throwsInvalidInput() {
        User user = unverifiedUser();
        user.setVerified(true);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> verificationUseCase.initVerification(EMAIL))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void initVerification_normalizesEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(unverifiedUser()));

        verificationUseCase.initVerification("  NEW@ESCUELAING.EDU.CO  ");

        verify(userRepository).findByEmail(EMAIL);
    }
}
