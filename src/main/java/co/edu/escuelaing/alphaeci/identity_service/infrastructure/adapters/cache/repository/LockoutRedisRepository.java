package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository;

import org.springframework.data.repository.CrudRepository;

import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.LockoutCache;

/**
 * Spring Data Redis repository for {@link LockoutCache}.
 * Provides CRUD operations for failed-login lockout entries keyed by email.
 */
public interface LockoutRedisRepository extends CrudRepository<LockoutCache, String> {
}
