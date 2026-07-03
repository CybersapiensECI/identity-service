package co.edu.escuelaing.alphaeci.identity_service.infrastructure.external;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.RegistrationProfile;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EventPublisherPort;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.external.dto.UserVerifiedEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisherAdapter implements EventPublisherPort {

    @Value("${rabbitmq.exchange.identity:identity.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.user-verified:identity.event.user-verified}")
    private String userVerifiedKey;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishUserVerified(User user, RegistrationProfile profile) {
        try {
            rabbitTemplate.convertAndSend(exchange, userVerifiedKey,
                    new UserVerifiedEventDto(
                            user.getId(),
                            user.getEmail().getValue(),
                            user.getRole().name(),
                            profile.name(),
                            profile.gender(),
                            profile.career(),
                            profile.semester(),
                            profile.studentCarnet(),
                            profile.photoUrl(),
                            profile.biography(),
                            profile.privacyLevel(),
                            profile.dateOfBirth(),
                            profile.geolocationEnabled()));
        } catch (Exception e) {
            log.warn("[DEV] RabbitMQ unavailable — event user-verified not published for user {}. Error: {}",
                    user.getId(), e.getMessage());
        }
    }

}
