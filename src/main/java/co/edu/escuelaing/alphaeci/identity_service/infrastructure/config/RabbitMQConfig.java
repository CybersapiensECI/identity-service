package co.edu.escuelaing.alphaeci.identity_service.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
