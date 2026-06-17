package co.edu.escuelaing.alphaeci.identity_service.domain.ports.in;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.response.LoginResponseDto;

public interface LoginPort {
    LoginResponseDto login(String email, String password);
    void logout(String token);
    LoginResponseDto refresh(String refreshToken);
}
