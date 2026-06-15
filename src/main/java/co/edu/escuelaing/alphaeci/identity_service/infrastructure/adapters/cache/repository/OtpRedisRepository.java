package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository;

import org.springframework.data.repository.CrudRepository;

import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.OtpCache;

/**
 * Spring Data Redis repository for {@link OtpCache}.
 * Provides CRUD operations for registration OTP entries keyed by email.
 */
public interface OtpRedisRepository extends CrudRepository<OtpCache, String> {
}
