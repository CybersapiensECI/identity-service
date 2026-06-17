package co.edu.escuelaing.alphaeci.identity_service.infrastructure.external;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.external.dto.OtpEventDto;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.external.dto.PasswordResetEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSenderAdapter implements EmailSenderPort {

    private static final String SEPARATOR = "============================================";

    @Value("${rabbitmq.exchange.identity:identity.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.otp:identity.email.otp}")
    private String otpKey;

    @Value("${rabbitmq.routing-key.password-reset:identity.email.password-reset}")
    private String passwordResetKey;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendOtp(String email, String code) {
        log.info(SEPARATOR);
        log.info("[EMAIL] OTP for {}: {}", email, code);
        log.info(SEPARATOR);
        try {
            rabbitTemplate.convertAndSend(exchange, otpKey, new OtpEventDto(email, code));
        } catch (Exception e) {
            log.warn("[DEV] RabbitMQ unavailable — OTP not sent by email. Use the code from the log above. Error: {}", e.getMessage());
        }
    }

    @Override
    public void sendPasswordReset(String email, String code) {
        log.info(SEPARATOR);
        log.info("[EMAIL] Password reset code for {}: {}", email, code);
        log.info(SEPARATOR);
        try {
            rabbitTemplate.convertAndSend(exchange, passwordResetKey, new PasswordResetEventDto(email, code));
        } catch (Exception e) {
            log.warn("[DEV] RabbitMQ unavailable — reset code not sent by email. Use the code from the log above. Error: {}", e.getMessage());
        }
    }
}
