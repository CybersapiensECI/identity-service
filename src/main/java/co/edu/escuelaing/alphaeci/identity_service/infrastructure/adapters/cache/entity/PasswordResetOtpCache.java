package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * Redis cache entity that stores a password-reset OTP for an email address.
 * Entries expire automatically after 10 minutes (600 s).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "password_reset_otp", timeToLive = 600)
public class PasswordResetOtpCache {

    /** Email address used as the Redis key. */
    @Id
    private String email;

    /** The 6-digit recovery code sent to the user. */
    private String code;

    /** Whether the recovery code has already been consumed. */
    private boolean used;

    /** Number of failed validation attempts for this code. */
    private int attempts;
}
