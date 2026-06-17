package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;

public interface JwtProviderPort {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    boolean validateToken(String token);

    String extractUserId(String token);

}