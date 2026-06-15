package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class TokenInvalidException extends IdentityServiceException {

    public TokenInvalidException(String message) {
        super(message, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

}
