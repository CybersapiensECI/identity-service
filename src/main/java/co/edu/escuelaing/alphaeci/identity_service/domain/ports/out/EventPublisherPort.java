package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;

public interface EventPublisherPort {

    void publishUserRegistered(User user);

    void publishUserVerified(User user);

    void publishPasswordChanged(User user);

}