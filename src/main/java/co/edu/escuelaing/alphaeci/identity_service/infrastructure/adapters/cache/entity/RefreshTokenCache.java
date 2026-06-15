package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

/**
 * Redis cache entity that stores an active user session (JWT + refresh token pair).
 * Entries expire automatically after 7 days (604 800 s).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "refresh_token", timeToLive = 604800)
public class RefreshTokenCache {

    /** Refresh token string used as the Redis key. */
    @Id
    private String refreshToken;

    /** User ID indexed for lookup by userId without knowing the token. */
    @Indexed
    private String userId;

    /** Email of the authenticated user. */
    private String email;

    /** Short-lived JWT access token associated with this session. */
    private String jwt;

    /** Whether this session has been explicitly revoked (e.g. by logout). */
    private boolean revoked;

    /** Timestamp when the session was created. */
    private LocalDateTime createdAt;

    /** Timestamp when the refresh token itself expires. */
    private LocalDateTime expiraRefresh;
}
