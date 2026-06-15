package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * Redis cache entity that records consecutive failed login attempts for an email address.
 * Entries expire automatically after 30 minutes (1800 s) — the same duration as the account lock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "lockout", timeToLive = 1800)
public class LockoutCache {

    /** Email address used as the Redis key. */
    @Id
    private String email;

    /** Number of consecutive failed login attempts for this email. */
    private int failedAttempts;
}
