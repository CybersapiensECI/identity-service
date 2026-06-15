package co.edu.escuelaing.alphaeci.identity_service.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

public class ValidateOtpRequestDto {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[^@]+@mail\\.escuelaing\\.edu\\.co$", message = "Email must be a valid @mail.escuelaing.edu.co address")
    @Schema(example = "usuario@mail.escuelaing.edu.co")
    private String email;
    @NotBlank(message = "OTP code must not be blank")
    @Pattern(regexp = "^\\d{6}$", message = "OTP code must be a 6-digit number")
    private String code;
}
