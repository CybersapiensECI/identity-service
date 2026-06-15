package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;

import java.util.Optional;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;

public interface UserRepositoryPort {
    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    void update(User user);
}