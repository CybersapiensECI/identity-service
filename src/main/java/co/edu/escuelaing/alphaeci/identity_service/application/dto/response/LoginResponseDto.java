package co.edu.escuelaing.alphaeci.identity_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after a successful login or token refresh.
 * Contains the short-lived JWT access token and the long-lived refresh token.
 */
@Schema(description = "Token pair returned upon successful authentication or token refresh")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    @Schema(description = "Short-lived JWT access token (15-minute expiry). Send in the Authorization header as: Bearer <accessToken>",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLTEyMyJ9.abc123")
    private String accessToken;

    @Schema(description = "Long-lived refresh token (7-day expiry). Use with POST /api/v1/auth/refresh to obtain new token pairs without re-authenticating.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJyZWZyZXNoLTQ1NiJ9.xyz789")
    private String refreshToken;

    @Schema(description = "Token scheme to use in the Authorization header. Always 'Bearer'.",
            example = "Bearer")
    private String tokenType;
}