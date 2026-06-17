package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class InvalidInputException extends IdentityServiceException {

    public InvalidInputException(String message) {
        super(message, org.springframework.http.HttpStatus.BAD_REQUEST);
    }
    
}
