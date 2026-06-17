package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class InvalidCredentialsException extends IdentityServiceException {

    public InvalidCredentialsException(String message) {
        super(message, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

}
