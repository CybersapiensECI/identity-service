package co.edu.escuelaing.alphaeci.identity_service.infrastructure.external.dto;

/**
 * Password reset event consumed by the notification-service, which is the component that actually
 * sends the email.
 *
 * <p>The field names are the contract: they must match {@code AuthNotificationEvent} on the consumer
 * side. {@code email} is where the code gets delivered — the notification-service has no user
 * directory and cannot resolve an address from {@code targetUserId}.
 */
public record PasswordResetEventDto(String targetUserId, String email, String type, String otp) {

    public static PasswordResetEventDto of(String userId, String email, String code) {
        return new PasswordResetEventDto(userId, email, "PASSWORD_RESET", code);
    }
}
