package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.OtpCache;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.PasswordResetOtpCache;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository.OtpRedisRepository;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository.PasswordResetOtpRedisRepository;

@ExtendWith(MockitoExtension.class)
class OtpRepositoryAdapterTest {

    @Mock private OtpRedisRepository otpRedisRepository;
    @Mock private PasswordResetOtpRedisRepository passwordResetOtpRedisRepository;

    @InjectMocks private OtpRepositoryAdapter adapter;

    private static final String EMAIL = "user@mail.escuelaing.edu.co";
    private static final long TTL = 600_000L;

    private OtpEmbedded sampleOtp() {
        return new OtpEmbedded("123456", TTL);
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void save_emailVerification_savesToOtpRepository() {
        adapter.save(EMAIL, sampleOtp(), OtpType.EMAIL_VERIFICATION);

        ArgumentCaptor<OtpCache> captor = ArgumentCaptor.forClass(OtpCache.class);
        verify(otpRedisRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(captor.getValue().getCode()).isEqualTo("123456");
    }

    @Test
    void save_passwordReset_savesToPasswordResetRepository() {
        adapter.save(EMAIL, sampleOtp(), OtpType.PASSWORD_RESET);

        ArgumentCaptor<PasswordResetOtpCache> captor = ArgumentCaptor.forClass(PasswordResetOtpCache.class);
        verify(passwordResetOtpRedisRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(captor.getValue().getCode()).isEqualTo("123456");
    }

    @Test
    void save_preservesAttemptsAndUsed() {
        OtpEmbedded otp = sampleOtp();
        otp.incrementAttempts();
        otp.setUsed(true);

        adapter.save(EMAIL, otp, OtpType.EMAIL_VERIFICATION);

        ArgumentCaptor<OtpCache> captor = ArgumentCaptor.forClass(OtpCache.class);
        verify(otpRedisRepository).save(captor.capture());
        assertThat(captor.getValue().getAttempts()).isEqualTo(1);
        assertThat(captor.getValue().isUsed()).isTrue();
    }

    // ── findByEmail ───────────────────────────────────────────────────────────

    @Test
    void findByEmail_emailVerification_found_returnsMappedOtp() {
        OtpCache cache = OtpCache.builder()
                .email(EMAIL).code("654321").used(false).attempts(0).build();
        when(otpRedisRepository.findById(EMAIL)).thenReturn(Optional.of(cache));

        Optional<OtpEmbedded> result = adapter.findByEmail(EMAIL, OtpType.EMAIL_VERIFICATION);

        assertThat(result).isPresent();
        assertThat(result.get().getCode().value()).isEqualTo("654321");
        assertThat(result.get().getAttempts()).isZero();
    }

    @Test
    void findByEmail_emailVerification_notFound_returnsEmpty() {
        when(otpRedisRepository.findById(EMAIL)).thenReturn(Optional.empty());

        assertThat(adapter.findByEmail(EMAIL, OtpType.EMAIL_VERIFICATION)).isEmpty();
    }

    @Test
    void findByEmail_passwordReset_found_returnsMappedOtp() {
        PasswordResetOtpCache cache = PasswordResetOtpCache.builder()
                .email(EMAIL).code("111222").used(true).attempts(2).build();
        when(passwordResetOtpRedisRepository.findById(EMAIL)).thenReturn(Optional.of(cache));

        Optional<OtpEmbedded> result = adapter.findByEmail(EMAIL, OtpType.PASSWORD_RESET);

        assertThat(result).isPresent();
        assertThat(result.get().getCode().value()).isEqualTo("111222");
        assertThat(result.get().getAttempts()).isEqualTo(2);
        assertThat(result.get().getUsed()).isTrue();
    }

    @Test
    void findByEmail_passwordReset_notFound_returnsEmpty() {
        when(passwordResetOtpRedisRepository.findById(EMAIL)).thenReturn(Optional.empty());

        assertThat(adapter.findByEmail(EMAIL, OtpType.PASSWORD_RESET)).isEmpty();
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_emailVerification_deletesFromOtpRepository() {
        adapter.delete(EMAIL, OtpType.EMAIL_VERIFICATION);
        verify(otpRedisRepository).deleteById(EMAIL);
    }

    @Test
    void delete_passwordReset_deletesFromPasswordResetRepository() {
        adapter.delete(EMAIL, OtpType.PASSWORD_RESET);
        verify(passwordResetOtpRedisRepository).deleteById(EMAIL);
    }
}
