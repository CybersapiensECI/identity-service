package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.RegistrationProfile;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;

public interface EventPublisherPort {

    void publishUserVerified(User user, RegistrationProfile profile);

}