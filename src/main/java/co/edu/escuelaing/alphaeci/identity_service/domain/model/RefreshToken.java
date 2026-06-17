package co.edu.escuelaing.alphaeci.identity_service.domain.model;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Domain model representing an active user session with JWT and refresh token.
 */
@Data
public class RefreshToken {

    private String id;
    private String userId;
    private String token;

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    private boolean revoked;

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void revoke() {
        this.revoked = true;
    }
}