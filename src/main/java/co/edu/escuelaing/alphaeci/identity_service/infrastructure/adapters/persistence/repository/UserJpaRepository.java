package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.entity.UserEntity;

@Repository
public interface UserJpaRepository
        extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);

}
