package co.edu.escuelaing.alphaeci.identity_service.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class LoginRequestDto {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[^@]+@mail\\.escuelaing\\.edu\\.co$", message = "Email must be a valid @mail.escuelaing.edu.co address")
    @Schema(example = "usuario@mail.escuelaing.edu.co")
    private String email;
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$,.]).{8,100}$", message = "Password must be at least 8 characters, contain at least one uppercase letter and one of: !@#$,.")
    private String password;
}
