package co.edu.escuelaing.alphaeci.identity_service.domain.ports.in;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ChangePasswordRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ResetPasswordRequestDto;

public interface PasswordPort {
    void changePassword(ChangePasswordRequestDto dto);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequestDto dto);
}
