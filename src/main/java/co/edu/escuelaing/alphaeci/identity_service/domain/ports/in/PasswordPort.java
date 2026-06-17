package co.edu.escuelaing.alphaeci.identity_service.domain.ports.in;

public interface PasswordPort {
    void changePassword(String accessToken, String currentPassword, String newPassword);
    void forgotPassword(String email);
    void resetPassword(String email, String code, String newPassword);
}
