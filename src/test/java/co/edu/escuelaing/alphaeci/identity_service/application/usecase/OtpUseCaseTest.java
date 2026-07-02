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

import co.edu.escuelaing.alphaeci.identity_service.application.dto.response.LoginResponseDto;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpExpiredException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpInvalidException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpMaxAttemptsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.Role;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.JwtProviderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.RefreshTokenRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;

@ExtendWith(MockitoExtension.class)
class OtpUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private OtpRepositoryPort otpRepository;
    @Mock private EmailSenderPort emailSender;
    @Mock private JwtProviderPort jwtProvider;
    @Mock private RefreshTokenRepositoryPort refreshTokenRepository;

    @InjectMocks private OtpUseCase otpUseCase;

    private static final String EMAIL = "student@escuelaing.edu.co";
    private static final long TEN_MINUTES_MS = 10 * 60 * 1000L;

    private User pendingUser() {
        return User.builder()
                .id("user-1")
                .email(new Email(EMAIL))
                .password("$2a$10$hash")
                .role(Role.STUDENT)
                .status(AccountStatus.PENDING_VERIFICATION)
                .build();
    }

    // ── resendOtp ──────────────────────────────────────────

    @Test
    void resendOtp_success_savesOtpAndSendsEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(pendingUser()));

        otpUseCase.resendOtp(EMAIL);

        verify(otpRepository).save(eq(EMAIL), any(OtpEmbedded.class), eq(OtpType.EMAIL_VERIFICATION));
        verify(emailSender).sendOtp(eq(EMAIL), anyString());
    }

    @Test
    void resendOtp_normalizesEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(pendingUser()));

        otpUseCase.resendOtp("  STUDENT@ESCUELAING.EDU.CO  ");

        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    void resendOtp_userNotFound_throwsInvalidInput() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpUseCase.resendOtp(EMAIL))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void resendOtp_alreadyVerified_throwsInvalidInput() {
        User user = pendingUser();
        user.setVerified(true);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> otpUseCase.resendOtp(EMAIL))
                .isInstanceOf(InvalidInputException.class);
    }

    // ── validateOtp ────────────────────────────────────────

    @Test
    void validateOtp_success_activatesUserAndReturnsTokens() {
        User user = pendingUser();
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        when(otpRepository.findByEmail(EMAIL, OtpType.EMAIL_VERIFICATION)).thenReturn(Optional.of(otp));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(any())).thenReturn("access-tok");
        when(jwtProvider.generateRefreshToken(any())).thenReturn("refresh-tok");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginResponseDto result = otpUseCase.validateOtp(EMAIL, "123456");

        assertThat(result.getAccessToken()).isEqualTo("access-tok");
        assertThat(user.isVerified()).isTrue();
        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(otpRepository).delete(EMAIL, OtpType.EMAIL_VERIFICATION);
    }

    @Test
    void validateOtp_invalidCodeFormat_throwsOtpInvalid() {
        assertThatThrownBy(() -> otpUseCase.validateOtp(EMAIL, "abc"))
                .isInstanceOf(OtpInvalidException.class);
    }

    @Test
    void validateOtp_otpNotFound_throwsOtpExpired() {
        when(otpRepository.findByEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpUseCase.validateOtp(EMAIL, "123456"))
                .isInstanceOf(OtpExpiredException.class);
    }

    @Test
    void validateOtp_expiredOtp_throwsOtpExpired() {
        OtpEmbedded expiredOtp = new OtpEmbedded("123456", -1000L);
        when(otpRepository.findByEmail(any(), any())).thenReturn(Optional.of(expiredOtp));

        assertThatThrownBy(() -> otpUseCase.validateOtp(EMAIL, "123456"))
                .isInstanceOf(OtpExpiredException.class);
    }

    @Test
    void validateOtp_wrongCode_incrementsAttemptsAndThrowsOtpInvalid() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        when(otpRepository.findByEmail(any(), any())).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> otpUseCase.validateOtp(EMAIL, "999999"))
                .isInstanceOf(OtpInvalidException.class);

        assertThat(otp.getAttempts()).isEqualTo(1);
        verify(otpRepository).save(eq(EMAIL), eq(otp), eq(OtpType.EMAIL_VERIFICATION));
    }

    @Test
    void validateOtp_maxAttemptsReached_deletesOtpAndThrowsMaxAttempts() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        otp.setAttempts(2);
        when(otpRepository.findByEmail(any(), any())).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> otpUseCase.validateOtp(EMAIL, "999999"))
                .isInstanceOf(OtpMaxAttemptsException.class);

        verify(otpRepository).delete(EMAIL, OtpType.EMAIL_VERIFICATION);
    }
}
