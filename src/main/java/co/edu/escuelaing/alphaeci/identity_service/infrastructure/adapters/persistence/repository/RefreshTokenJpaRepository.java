package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.entity.RefreshTokenEntity;

@Repository
public interface RefreshTokenJpaRepository
        extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByToken(String token);

    List<RefreshTokenEntity> findByUserId(String userId);

}
