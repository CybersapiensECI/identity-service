package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

import org.springframework.http.HttpStatus;


public class IdentityServiceException extends RuntimeException {
    
    private final HttpStatus status;

    public IdentityServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}