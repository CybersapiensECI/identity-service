package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;

public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);

}
