package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class OtpMaxAttemptsException extends IdentityServiceException {

    public OtpMaxAttemptsException(String message) {
        super(message, org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
    }

}
