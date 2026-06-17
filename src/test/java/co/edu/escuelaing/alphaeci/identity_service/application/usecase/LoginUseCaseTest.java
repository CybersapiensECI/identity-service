package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.response.LoginResponseDto;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.AccountBlocked;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.EmailNotVerifiedException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidCredentialsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.TokenExpiredException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.TokenInvalidException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.RefreshToken;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.Role;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.JwtProviderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.LockoutRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.PasswordEncoderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.RefreshTokenRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.PasswordHash;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock private LockoutRepositoryPort lockoutRepository;
    @Mock private JwtProviderPort jwtProvider;
    @Mock private PasswordEncoderPort passwordEncoder;

    @InjectMocks private LoginUseCase loginUseCase;

    private static final String TEST_EMAIL = "test@escuelaing.edu.co";
    private static final String ENCODED_PASS = "$2a$10$encodedHash";

    private User activeVerifiedUser() {
        User user = new User();
        user.setId("user-123");
        user.setEmail(new Email(TEST_EMAIL));
        user.setPassword(PasswordHash.fromEncoded(ENCODED_PASS));
        user.setRole(Role.STUDENT);
        user.setStatus(AccountStatus.ACTIVE);
        user.setVerified(true);
        return user;
    }

    private RefreshToken storedSession(String tokenValue) {
        RefreshToken rt = new RefreshToken();
        rt.setId("session-1");
        rt.setUserId("user-123");
        rt.setToken(tokenValue);
        rt.setRevoked(false);
        rt.setCreatedAt(LocalDateTime.now());
        rt.setExpiresAt(LocalDateTime.now().plusDays(7));
        return rt;
    }

    // ── login ──────────────────────────────────────────────

    @Test
    void login_success_returnsAccessAndRefreshTokens() {
        User user = activeVerifiedUser();
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(lockoutRepository.findFailedAttempts(TEST_EMAIL)).thenReturn(0);
        when(passwordEncoder.matches("Pass1!", ENCODED_PASS)).thenReturn(true);
        when(jwtProvider.generateAccessToken(user)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(user)).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginResponseDto result = loginUseCase.login(TEST_EMAIL, "Pass1!");

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        verify(lockoutRepository).clearFailedAttempts(TEST_EMAIL);
    }

    @Test
    void login_normalizesEmailBeforeLookup() {
        User user = activeVerifiedUser();
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(lockoutRepository.findFailedAttempts(TEST_EMAIL)).thenReturn(0);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtProvider.generateAccessToken(any())).thenReturn("tok");
        when(jwtProvider.generateRefreshToken(any())).thenReturn("ref");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        loginUseCase.login("  TEST@ESCUELAING.EDU.CO  ", "Pass1!");

        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.login(TEST_EMAIL, "Pass1!"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_maxAttemptsReached_throwsAccountBlocked() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(activeVerifiedUser()));
        when(lockoutRepository.findFailedAttempts(TEST_EMAIL)).thenReturn(5);

        assertThatThrownBy(() -> loginUseCase.login(TEST_EMAIL, "Pass1!"))
                .isInstanceOf(AccountBlocked.class);
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsAndIncrementsAttempts() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(activeVerifiedUser()));
        when(lockoutRepository.findFailedAttempts(any())).thenReturn(0);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.login(TEST_EMAIL, "WrongPass!"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(lockoutRepository).incrementFailedAttempts(TEST_EMAIL);
    }

    @Test
    void login_emailNotVerified_throwsEmailNotVerifiedException() {
        User user = activeVerifiedUser();
        user.setVerified(false);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(lockoutRepository.findFailedAttempts(any())).thenReturn(0);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> loginUseCase.login(TEST_EMAIL, "Pass1!"))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void login_blockedAccount_throwsAccountBlocked() {
        User user = activeVerifiedUser();
        user.setStatus(AccountStatus.BLOCKED);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(lockoutRepository.findFailedAttempts(any())).thenReturn(0);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> loginUseCase.login(TEST_EMAIL, "Pass1!"))
                .isInstanceOf(AccountBlocked.class);
    }

    // ── logout ─────────────────────────────────────────────

    @Test
    void logout_validToken_revokesIt() {
        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(storedSession("valid-token")));

        loginUseCase.logout("valid-token");

        verify(refreshTokenRepository).revoke("valid-token");
    }

    @Test
    void logout_tokenNotFound_throwsTokenInvalid() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.logout("unknown"))
                .isInstanceOf(TokenInvalidException.class);
    }

    // ── refresh ────────────────────────────────────────────

    @Test
    void refresh_validToken_returnsNewTokenPair() {
        User user = activeVerifiedUser();
        RefreshToken session = storedSession("old-refresh");
        when(refreshTokenRepository.findByToken("old-refresh")).thenReturn(Optional.of(session));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(user)).thenReturn("new-access");
        when(jwtProvider.generateRefreshToken(user)).thenReturn("new-refresh");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginResponseDto result = loginUseCase.refresh("old-refresh");

        assertThat(result.getAccessToken()).isEqualTo("new-access");
        verify(refreshTokenRepository).revoke("old-refresh");
    }

    @Test
    void refresh_tokenNotFound_throwsTokenInvalid() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.refresh("bad-token"))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void refresh_revokedToken_throwsTokenExpired() {
        RefreshToken session = storedSession("rev-token");
        session.setRevoked(true);
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> loginUseCase.refresh("rev-token"))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    void refresh_expiredToken_throwsTokenExpired() {
        RefreshToken session = storedSession("exp-token");
        session.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> loginUseCase.refresh("exp-token"))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    void refresh_blockedUser_throwsAccountBlocked() {
        User user = activeVerifiedUser();
        user.setStatus(AccountStatus.BLOCKED);
        RefreshToken session = storedSession("ref-token");
        when(refreshTokenRepository.findByToken(eq("ref-token"))).thenReturn(Optional.of(session));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loginUseCase.refresh("ref-token"))
                .isInstanceOf(AccountBlocked.class);
    }
}
