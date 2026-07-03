package co.edu.escuelaing.alphaeci.identity_service.infrastructure.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.TokenInvalidException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.Role;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "TestSecretKeyThatIsLongEnoughForHS256Algorithm!";
    private static final long ACCESS_EXPIRATION = 900_000L;
    private static final long REFRESH_EXPIRATION = 604_800_000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessExpiration", ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);
    }

    private User testUser() {
        return User.builder()
                .id("user-123")
                .email(new Email("test@mail.escuelaing.edu.co"))
                .password("$2a$10$hash")
                .role(Role.STUDENT)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void generateAccessToken_returnsNonNullToken() {
        String token = jwtService.generateAccessToken(testUser());
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateRefreshToken_returnsNonNullToken() {
        String token = jwtService.generateRefreshToken(testUser());
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void validateToken_validAccessToken_returnsTrue() {
        String token = jwtService.generateAccessToken(testUser());
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_validRefreshToken_returnsTrue() {
        String token = jwtService.generateRefreshToken(testUser());
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertThat(jwtService.validateToken("not.a.valid.token")).isFalse();
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtService.generateAccessToken(testUser()) + "tampered";
        assertThat(jwtService.validateToken(token)).isFalse();
    }

    @Test
    void extractUserId_validToken_returnsCorrectId() {
        User user = testUser();
        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.extractUserId(token)).isEqualTo("user-123");
    }

    @Test
    void extractUserId_invalidToken_throwsTokenInvalid() {
        assertThatThrownBy(() -> jwtService.extractUserId("bad.token.here"))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void extractRole_validToken_returnsCorrectRole() {
        String token = jwtService.generateAccessToken(testUser());
        assertThat(jwtService.extractRole(token)).isEqualTo(Role.STUDENT.name());
    }

    @Test
    void extractRole_invalidToken_throwsTokenInvalid() {
        assertThatThrownBy(() -> jwtService.extractRole("bad.token.here"))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void accessToken_andRefreshToken_areDistinct() {
        User user = testUser();
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        assertThat(access).isNotEqualTo(refresh);
    }
}
