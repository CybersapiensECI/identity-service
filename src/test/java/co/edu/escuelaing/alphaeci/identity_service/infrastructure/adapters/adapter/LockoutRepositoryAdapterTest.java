package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.LockoutCache;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository.LockoutRedisRepository;

@ExtendWith(MockitoExtension.class)
class LockoutRepositoryAdapterTest {

    @Mock private LockoutRedisRepository lockoutRedisRepository;

    @InjectMocks private LockoutRepositoryAdapter adapter;

    private static final String EMAIL = "user@mail.escuelaing.edu.co";

    // ── findFailedAttempts ────────────────────────────────────────────────────

    @Test
    void findFailedAttempts_entryExists_returnsCount() {
        LockoutCache cache = LockoutCache.builder().email(EMAIL).failedAttempts(3).build();
        when(lockoutRedisRepository.findById(EMAIL)).thenReturn(Optional.of(cache));

        assertThat(adapter.findFailedAttempts(EMAIL)).isEqualTo(3);
    }

    @Test
    void findFailedAttempts_noEntry_returnsZero() {
        when(lockoutRedisRepository.findById(EMAIL)).thenReturn(Optional.empty());

        assertThat(adapter.findFailedAttempts(EMAIL)).isZero();
    }

    // ── incrementFailedAttempts ───────────────────────────────────────────────

    @Test
    void incrementFailedAttempts_existingEntry_incrementsCount() {
        LockoutCache existing = LockoutCache.builder().email(EMAIL).failedAttempts(2).build();
        when(lockoutRedisRepository.findById(EMAIL)).thenReturn(Optional.of(existing));

        adapter.incrementFailedAttempts(EMAIL);

        verify(lockoutRedisRepository).save(argThat(c -> c.getFailedAttempts() == 3));
    }

    @Test
    void incrementFailedAttempts_noEntry_createsNewWithCountOne() {
        when(lockoutRedisRepository.findById(EMAIL)).thenReturn(Optional.empty());

        adapter.incrementFailedAttempts(EMAIL);

        verify(lockoutRedisRepository).save(argThat(c -> c.getFailedAttempts() == 1 && EMAIL.equals(c.getEmail())));
    }

    // ── clearFailedAttempts ───────────────────────────────────────────────────

    @Test
    void clearFailedAttempts_deletesEntry() {
        adapter.clearFailedAttempts(EMAIL);

        verify(lockoutRedisRepository).deleteById(EMAIL);
    }
}
