package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.RefreshToken;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.RefreshTokenRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.mapper.RefreshTokenMapper;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository repository;
    private final RefreshTokenMapper mapper;

    @Override
    public RefreshToken save(RefreshToken token) {
        return mapper.toDomain(repository.save(mapper.toEntity(token)));
    }

    @Override
    public Optional<RefreshToken> findByToken(String refreshToken) {
        return repository.findByToken(refreshToken).map(mapper::toDomain);
    }

    @Override
    public void deleteByUserId(String userId) {
        repository.deleteAll(repository.findByUserId(userId));
    }

    @Override
    public void revoke(String token) {
        repository.findByToken(token).ifPresent(e -> {
            e.setRevoked(true);
            repository.save(e);
        });
    }
}
