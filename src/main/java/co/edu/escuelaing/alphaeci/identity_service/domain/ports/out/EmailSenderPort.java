package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;


public interface EmailSenderPort {
    void sendOtp(String userId, String email, String code);

    void sendPasswordReset(String userId, String email, String code);

    void sendVerificationSuccess(String email);
}
