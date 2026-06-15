package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class EmailNotVerifiedException extends IdentityServiceException {

    public EmailNotVerifiedException(String message) {
        super(message, org.springframework.http.HttpStatus.FORBIDDEN);
    }

}
