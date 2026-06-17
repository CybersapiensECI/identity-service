package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class UserAlreadyExistsException extends IdentityServiceException {

    public UserAlreadyExistsException(String message) {
        super(message, org.springframework.http.HttpStatus.CONFLICT);
    }

}
