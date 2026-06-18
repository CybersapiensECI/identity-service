package co.edu.escuelaing.alphaeci.identity_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerificationRequestDto {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[^@]+@mail\\.escuelaing\\.edu\\.co$", message = "Email must be a valid @mail.escuelaing.edu.co address")
    @Schema(example = "usuario@mail.escuelaing.edu.co")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(example = "SecurePass123!")
    private String password;
}
