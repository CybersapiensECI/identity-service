package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class OtpExpiredException extends IdentityServiceException {

    public OtpExpiredException(String message) {
        super(message, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

}
