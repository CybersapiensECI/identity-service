package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
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

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidCredentialsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpInvalidException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpMaxAttemptsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.Role;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.JwtProviderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.PasswordEncoderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.PasswordHash;

@ExtendWith(MockitoExtension.class)
class PasswordUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordEncoderPort passwordEncoder;
    @Mock private OtpRepositoryPort otpRepository;
    @Mock private EmailSenderPort emailSender;
    @Mock private JwtProviderPort jwtProvider;

    @InjectMocks private PasswordUseCase passwordUseCase;

    private static final String EMAIL = "user@escuelaing.edu.co";
    private static final String ENCODED = "$2a$10$encodedHash";
    private static final long FIFTEEN_MINUTES_MS = 15 * 60 * 1000L;

    private User activeUser() {
        User user = new User();
        user.setId("uid-1");
        user.setEmail(new Email(EMAIL));
        user.setPassword(PasswordHash.fromEncoded(ENCODED));
        user.setRole(Role.STUDENT);
        user.setStatus(AccountStatus.ACTIVE);
        user.setVerified(true);
        return user;
    }

    // ── changePassword ─────────────────────────────────────

    @Test
    void changePassword_success_updatesPasswordInRepository() {
        User user = activeUser();
        when(jwtProvider.extractUserId("access-token")).thenReturn("uid-1");
        when(userRepository.findById("uid-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", ENCODED)).thenReturn(true);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("$2a$10$newHash");

        passwordUseCase.changePassword("access-token", "OldPass1!", "NewPass1!");

        verify(userRepository).update(user);
        assertThat(user.getPassword().getValue()).isEqualTo("$2a$10$newHash");
    }

    @Test
    void changePassword_userNotFound_throwsInvalidInput() {
        when(jwtProvider.extractUserId(any())).thenReturn("ghost");
        when(userRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordUseCase.changePassword("token", "OldPass1!", "NewPass1!"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsInvalidCredentials() {
        when(jwtProvider.extractUserId(any())).thenReturn("uid-1");
        when(userRepository.findById("uid-1")).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> passwordUseCase.changePassword("token", "WrongPass!", "NewPass1!"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void changePassword_weakNewPassword_throwsInvalidInput() {
        when(jwtProvider.extractUserId(any())).thenReturn("uid-1");
        when(userRepository.findById("uid-1")).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> passwordUseCase.changePassword("token", "OldPass1!", "weak"))
                .isInstanceOf(InvalidInputException.class);
    }

    // ── forgotPassword ─────────────────────────────────────

    @Test
    void forgotPassword_success_savesOtpAndSendsResetEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser()));

        passwordUseCase.forgotPassword(EMAIL);

        verify(otpRepository).save(eq(EMAIL), any(OtpEmbedded.class), eq(OtpType.PASSWORD_RESET));
        verify(emailSender).sendPasswordReset(eq(EMAIL), anyString());
    }

    @Test
    void forgotPassword_normalizesEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser()));

        passwordUseCase.forgotPassword("  USER@ESCUELAING.EDU.CO  ");

        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    void forgotPassword_userNotFound_throwsInvalidInput() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordUseCase.forgotPassword(EMAIL))
                .isInstanceOf(InvalidInputException.class);
    }

    // ── resetPassword ──────────────────────────────────────

    @Test
    void resetPassword_success_updatesUserPassword() {
        User user = activeUser();
        OtpEmbedded otp = new OtpEmbedded("654321", FIFTEEN_MINUTES_MS);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(otpRepository.findByEmail(EMAIL, OtpType.PASSWORD_RESET)).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("NewPass1!")).thenReturn("$2a$10$newHash");

        passwordUseCase.resetPassword(EMAIL, "654321", "NewPass1!");

        verify(otpRepository).delete(EMAIL, OtpType.PASSWORD_RESET);
        verify(userRepository).update(user);
        assertThat(user.getPassword().getValue()).isEqualTo("$2a$10$newHash");
    }

    @Test
    void resetPassword_userNotFound_throwsInvalidInput() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordUseCase.resetPassword(EMAIL, "123456", "NewPass1!"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void resetPassword_otpNotFound_throwsInvalidInput() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(activeUser()));
        when(otpRepository.findByEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordUseCase.resetPassword(EMAIL, "123456", "NewPass1!"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void resetPassword_expiredOtp_throwsInvalidInput() {
        OtpEmbedded expiredOtp = new OtpEmbedded("654321", -1000L);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(activeUser()));
        when(otpRepository.findByEmail(any(), any())).thenReturn(Optional.of(expiredOtp));

        assertThatThrownBy(() -> passwordUseCase.resetPassword(EMAIL, "654321", "NewPass1!"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void resetPassword_wrongCode_throwsOtpInvalid() {
        OtpEmbedded otp = new OtpEmbedded("654321", FIFTEEN_MINUTES_MS);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(activeUser()));
        when(otpRepository.findByEmail(any(), any())).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> passwordUseCase.resetPassword(EMAIL, "000000", "NewPass1!"))
                .isInstanceOf(OtpInvalidException.class);

        assertThat(otp.getAttempts()).isEqualTo(1);
    }

    @Test
    void resetPassword_maxAttemptsReached_deletesOtpAndThrowsMaxAttempts() {
        OtpEmbedded otp = new OtpEmbedded("654321", FIFTEEN_MINUTES_MS);
        otp.setAttempts(2);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(activeUser()));
        when(otpRepository.findByEmail(any(), any())).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> passwordUseCase.resetPassword(EMAIL, "000000", "NewPass1!"))
                .isInstanceOf(OtpMaxAttemptsException.class);

        verify(otpRepository).delete(EMAIL, OtpType.PASSWORD_RESET);
    }
}
