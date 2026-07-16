package co.edu.escuelaing.alphaeci.identity_service.infrastructure.external.dto;

/**
 * OTP event consumed by the notification-service, which is the component that actually sends the
 * email.
 *
 * <p>The field names are the contract: they must match {@code AuthNotificationEvent} on the consumer
 * side. {@code email} is where the code gets delivered — the notification-service has no user
 * directory and cannot resolve an address from {@code targetUserId}.
 */
public record OtpEventDto(String targetUserId, String email, String type, String otp) {

    public static OtpEventDto verification(String userId, String email, String code) {
        return new OtpEventDto(userId, email, "OTP_VERIFICATION", code);
    }
}
