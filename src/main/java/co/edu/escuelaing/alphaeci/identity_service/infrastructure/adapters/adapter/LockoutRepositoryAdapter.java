package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.adapter;

import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.LockoutRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.LockoutCache;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository.LockoutRedisRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockoutRepositoryAdapter implements LockoutRepositoryPort {

    private final LockoutRedisRepository lockoutRedisRepository;

    @Override
    public int findFailedAttempts(String email) {
        return lockoutRedisRepository.findById(email)
                .map(LockoutCache::getFailedAttempts)
                .orElse(0);
    }

    @Override
    public void incrementFailedAttempts(String email) {
        LockoutCache lockout = lockoutRedisRepository.findById(email)
                .orElse(LockoutCache.builder().email(email).failedAttempts(0).build());
        lockout.setFailedAttempts(lockout.getFailedAttempts() + 1);
        lockoutRedisRepository.save(lockout);
    }

    @Override
    public void clearFailedAttempts(String email) {
        lockoutRedisRepository.deleteById(email);
    }
}
