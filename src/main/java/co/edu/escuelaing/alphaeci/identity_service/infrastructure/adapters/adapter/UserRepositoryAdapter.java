package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.mapper.UserMapper;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public void update(User user) {
        repository.save(mapper.toEntity(user));
    }
}
