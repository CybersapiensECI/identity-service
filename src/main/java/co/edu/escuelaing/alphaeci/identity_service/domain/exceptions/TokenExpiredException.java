package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class TokenExpiredException extends IdentityServiceException {

    public TokenExpiredException(String message) {
        super(message, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

}
