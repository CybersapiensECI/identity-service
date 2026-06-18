package co.edu.escuelaing.alphaeci.identity_service.domain.ports.in;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.RegistrationProfile;

public interface VerificationPort {
    void initVerification(String email, String password);

    void completeRegistration(String email, RegistrationProfile profile);
}
