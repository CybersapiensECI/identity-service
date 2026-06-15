package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository;

import org.springframework.data.repository.CrudRepository;

import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.PasswordResetOtpCache;

/**
 * Spring Data Redis repository for {@link PasswordResetOtpCache}.
 * Provides CRUD operations for password-reset OTP entries keyed by email.
 */
public interface PasswordResetOtpRedisRepository extends CrudRepository<PasswordResetOtpCache, String> {
}
