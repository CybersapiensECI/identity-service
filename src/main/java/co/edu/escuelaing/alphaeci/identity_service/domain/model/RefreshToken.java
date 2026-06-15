package co.edu.escuelaing.alphaeci.identity_service.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model representing an active user session with JWT and refresh token.
 */
public class RefreshToken {

    private String id;
    private String userId;
    private String email;
    private String jwt;
    private String token;
    private LocalDateTime expiraJwt;
    private Boolean revocado;
    private LocalDateTime createdAt;
    private LocalDateTime expiraRefresh;

    /**
     * Creates a new refresh token session.
     *
     * @param id            unique session identifier
     * @param userId        ID of the authenticated user
     * @param email         email of the authenticated user
     * @param jwt           JWT access token
     * @param refreshToken  refresh token value
     * @param expiraJwt     expiration time of the access token
     * @param revocado      whether the session has been revoked
     * @param createdAt     session creation timestamp
     * @param expiraRefresh expiration time of the refresh token
     */
    public RefreshToken(String id, String userId, String email, String jwt, String refreshToken,
                        LocalDateTime expiraJwt, Boolean revocado,
                        LocalDateTime createdAt, LocalDateTime expiraRefresh) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.jwt = jwt;
        this.token = refreshToken;
        this.expiraJwt = expiraJwt;
        this.revocado = revocado;
        this.createdAt = createdAt;
        this.expiraRefresh = expiraRefresh;
    }

    /** Returns true if the refresh token has passed its expiration time. */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiraRefresh);
    }

    /** Marks this session as revoked. */
    public void revoke() {
        this.revocado = true;
    }

    /** Returns true if this session has been revoked. */
    public boolean isRevoked() {
        return Boolean.TRUE.equals(revocado);
    }

    /** @return unique session identifier */
    public String getId() { return id; }
    /** @return ID of the authenticated user */
    public String getUserId() { return userId; }
    /** @return email of the authenticated user */
    public String getEmail() { return email; }
    /** @return JWT access token */
    public String getJwt() { return jwt; }
    /** @return refresh token value */
    public String getRefreshToken() { return token; }
    /** @return expiration time of the access token */
    public LocalDateTime getExpiraJwt() { return expiraJwt; }
    /** @return whether the session is revoked */
    public Boolean getRevocado() { return revocado; }
    /** @return session creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /** @return expiration time of the refresh token */
    public LocalDateTime getExpiraRefresh() { return expiraRefresh; }

    /** @param revocado true to mark the session as revoked */
    public void setRevocado(Boolean revocado) { this.revocado = revocado; }
}