package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;


public interface EmailSenderPort {
    void sendOtp(String email, String code);

    void sendPasswordReset(String email, String code);
}
