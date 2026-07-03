package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;

import java.util.List;
import java.util.Optional;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.RefreshToken;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String refreshToken);

    void deleteByUserId(String userId);

    void revoke(String token);

}
