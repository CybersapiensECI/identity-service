package co.edu.escuelaing.alphaeci.identity_service.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.identity:identity.exchange}")
    private String exchange;

    @Value("${rabbitmq.exchange.notification:notification.exchange}")
    private String notificationExchange;

    @Bean
    public TopicExchange identityExchange() {
        return new TopicExchange(exchange, true, false);
    }

    /**
     * Owned by the notification-service; declared here so publishing works whichever service starts
     * first. Durable and non-auto-delete to match that declaration — publishing to an exchange that
     * does not exist drops the message silently.
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notificationExchange, true, false);
    }

    /**
     * Jackson2JsonMessageConverter builds its own ObjectMapper by default, which
     * does not have JavaTimeModule registered (unlike Spring Boot's autoconfigured
     * one). Without it, LocalDate fields such as UserVerifiedEventDto.dateOfBirth
     * serialize as [year, month, day] instead of an ISO string, breaking any
     * consumer that expects a plain date string.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
