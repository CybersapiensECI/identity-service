package co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object encapsulating an OTP code together with its expiry, usage state,
 * and attempt counter. Used for both email-verification and password-reset flows.
 */
@Data
@NoArgsConstructor
public class OtpEmbedded {

    private static final int MAX_ATTEMPTS = 3;

    private OtpCode code;
    private LocalDateTime expiresAt;
    private Boolean used;
    private Integer attempts;

    /**
     * Creates a new OTP ready to be stored.
     *
     * @param rawCode       the 6-digit code string
     * @param durationMillis how long the OTP is valid, in milliseconds
     */
    public OtpEmbedded(String rawCode, long durationMillis) {
        this.code = new OtpCode(rawCode);
        this.expiresAt = LocalDateTime.now().plusNanos(durationMillis * 1_000_000L);
        this.used = false;
        this.attempts = 0;
    }

    /**
     * @return {@code true} if the OTP has not been used and has not yet expired.
     * When {@code expiresAt} is null, expiry is delegated to Redis TTL.
     */
    public boolean isValid() {
        return !Boolean.TRUE.equals(used) && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }

    /** @return {@code true} if the expiry timestamp is in the past (false when null — Redis TTL is the authority) */
    public boolean hasExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /** Increments the failed-attempt counter by one. */
    public void incrementAttempts() {
        this.attempts = (this.attempts == null ? 0 : this.attempts) + 1;
    }

    /** @return {@code true} if the attempt count has reached or exceeded {@value MAX_ATTEMPTS} */
    public boolean hasReachedLimit() {
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }
}
