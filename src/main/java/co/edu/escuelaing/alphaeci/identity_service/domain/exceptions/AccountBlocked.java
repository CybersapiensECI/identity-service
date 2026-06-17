package co.edu.escuelaing.alphaeci.identity_service.domain.exceptions;

public class AccountBlocked extends IdentityServiceException {

    public AccountBlocked(String message) {
        super(message, org.springframework.http.HttpStatus.FORBIDDEN);
    }

}
