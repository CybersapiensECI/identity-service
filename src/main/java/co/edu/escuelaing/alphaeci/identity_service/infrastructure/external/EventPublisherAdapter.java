package co.edu.escuelaing.alphaeci.identity_service.infrastructure.external;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EventPublisherPort;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.external.dto.UserEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisherAdapter implements EventPublisherPort {

    @Value("${rabbitmq.exchange.identity:identity.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.user-registered:identity.event.user-registered}")
    private String userRegisteredKey;

    @Value("${rabbitmq.routing-key.user-verified:identity.event.user-verified}")
    private String userVerifiedKey;

    @Value("${rabbitmq.routing-key.password-changed:identity.event.password-changed}")
    private String passwordChangedKey;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishUserRegistered(User user) {
        publish(userRegisteredKey, user, "user-registered");
    }

    @Override
    public void publishUserVerified(User user) {
        publish(userVerifiedKey, user, "user-verified");
    }

    @Override
    public void publishPasswordChanged(User user) {
        publish(passwordChangedKey, user, "password-changed");
    }

    private void publish(String routingKey, User user, String eventType) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey,
                    new UserEventDto(user.getId(), user.getEmail().getValue(), user.getRole().name()));
        } catch (Exception e) {
            log.warn("[DEV] RabbitMQ unavailable — event {} not published for user {}. Error: {}",
                    eventType, user.getId(), e.getMessage());
        }
    }
}
