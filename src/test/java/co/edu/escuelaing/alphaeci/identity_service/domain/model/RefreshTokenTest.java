package co.edu.escuelaing.alphaeci.identity_service.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    private RefreshToken tokenWithExpiry(LocalDateTime expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setId("tok-1");
        token.setUserId("user-1");
        token.setToken("jwt-refresh-value");
        token.setRevoked(false);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(expiresAt);
        return token;
    }

    @Test
    void isExpired_expiryInPast_returnsTrue() {
        RefreshToken token = tokenWithExpiry(LocalDateTime.now().minusDays(1));
        assertThat(token.isExpired()).isTrue();
    }

    @Test
    void isExpired_expiryInFuture_returnsFalse() {
        RefreshToken token = tokenWithExpiry(LocalDateTime.now().plusDays(7));
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    void isExpired_nullExpiry_returnsFalse() {
        RefreshToken token = tokenWithExpiry(null);
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    void revoke_setsRevokedToTrue() {
        RefreshToken token = tokenWithExpiry(LocalDateTime.now().plusDays(7));
        assertThat(token.isRevoked()).isFalse();

        token.revoke();

        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    void newToken_isNotRevokedByDefault() {
        RefreshToken token = tokenWithExpiry(LocalDateTime.now().plusDays(7));
        assertThat(token.isRevoked()).isFalse();
    }
}
