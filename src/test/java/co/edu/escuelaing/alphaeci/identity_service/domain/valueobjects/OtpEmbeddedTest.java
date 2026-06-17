package co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class OtpEmbeddedTest {

    private static final long TEN_MINUTES_MS = 10 * 60 * 1000L;

    @Test
    void newOtp_isValidByDefault() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        assertThat(otp.isValid()).isTrue();
    }

    @Test
    void newOtp_hasNotExpired() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        assertThat(otp.hasExpired()).isFalse();
    }

    @Test
    void newOtp_hasZeroAttempts() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        assertThat(otp.getAttempts()).isZero();
    }

    @Test
    void expiredOtp_isNotValid() {
        OtpEmbedded otp = new OtpEmbedded("123456", -1000L);
        assertThat(otp.isValid()).isFalse();
    }

    @Test
    void expiredOtp_hasExpiredReturnsTrue() {
        OtpEmbedded otp = new OtpEmbedded("123456", -1000L);
        assertThat(otp.hasExpired()).isTrue();
    }

    @Test
    void usedOtp_isNotValid() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        otp.setUsed(true);
        assertThat(otp.isValid()).isFalse();
    }

    @Test
    void nullExpiresAt_isConsideredValid() {
        OtpEmbedded otp = new OtpEmbedded();
        otp.setUsed(false);
        otp.setAttempts(0);
        // expiresAt = null → Redis TTL is authority, treat as valid
        assertThat(otp.isValid()).isTrue();
    }

    @Test
    void incrementAttempts_increasesCountByOne() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        otp.incrementAttempts();
        assertThat(otp.getAttempts()).isEqualTo(1);
    }

    @Test
    void incrementAttempts_calledThreeTimes_reachesLimit() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        otp.incrementAttempts();
        otp.incrementAttempts();
        otp.incrementAttempts();
        assertThat(otp.hasReachedLimit()).isTrue();
    }

    @Test
    void hasReachedLimit_twoAttempts_returnsFalse() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        otp.setAttempts(2);
        assertThat(otp.hasReachedLimit()).isFalse();
    }

    @Test
    void hasReachedLimit_exactlyThreeAttempts_returnsTrue() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        otp.setAttempts(3);
        assertThat(otp.hasReachedLimit()).isTrue();
    }

    @Test
    void incrementAttempts_withNullAttempts_setsToOne() {
        OtpEmbedded otp = new OtpEmbedded();
        otp.setAttempts(null);
        otp.incrementAttempts();
        assertThat(otp.getAttempts()).isEqualTo(1);
    }

    @Test
    void otp_storedCode_matchesInput() {
        OtpEmbedded otp = new OtpEmbedded("987654", TEN_MINUTES_MS);
        assertThat(otp.getCode()).isEqualTo(OtpCode.of("987654"));
    }

    @Test
    void otp_expiresAt_isInTheFuture() {
        OtpEmbedded otp = new OtpEmbedded("123456", TEN_MINUTES_MS);
        assertThat(otp.getExpiresAt()).isAfter(LocalDateTime.now());
    }
}
