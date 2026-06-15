package co.edu.escuelaing.alphaeci.identity_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO used for operations that return only a status message
 * (e.g. OTP sent, logout, password changed).
 */
@Schema(description = "Generic confirmation response returned for operations that produce a status message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDto {

    @Schema(description = "Human-readable confirmation message describing the outcome of the operation", example = "OTP sent to email")
    private String message;
}