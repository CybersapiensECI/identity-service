package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class OtpInvalidException extends IdentityServiceException {

    public OtpInvalidException(String message) {
        super(message, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

}
