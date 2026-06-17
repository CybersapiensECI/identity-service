package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository;

import org.springframework.data.repository.CrudRepository;

import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.RefreshTokenCache;

import java.util.Optional;

/**
 * Spring Data Redis repository for {@link RefreshTokenCache}.
 * Provides CRUD operations for active user sessions keyed by refresh token.
 */
public interface RefreshTokenRedisRepository extends CrudRepository<RefreshTokenCache, String> {

    /**
     * Finds an active session by the user's ID.
     *
     * @param userId the user's unique identifier
     * @return the session cache entry, or empty if none exists
     */
    Optional<RefreshTokenCache> findByUserId(String userId);
}
