package co.edu.escuelaing.alphaeci.identity_service.application.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompleteRegistrationRequestDto {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[^@]+@mail\\.escuelaing\\.edu\\.co$", message = "Email must be a valid @mail.escuelaing.edu.co address")
    @Schema(example = "usuario@mail.escuelaing.edu.co")
    private String email;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Schema(example = "Juan Pérez")
    private String name;

    @NotBlank(message = "Gender is required")
    @Schema(description = "Allowed values: MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY", example = "MALE")
    private String gender;

    @NotBlank(message = "Career is required")
    @Schema(description = "Allowed values: SYSTEMS_ENGINEERING, CIVIL_ENGINEERING, INDUSTRIAL_ENGINEERING, ...", example = "SYSTEMS_ENGINEERING")
    private String career;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Minimum semester is 1")
    @Max(value = 10, message = "Maximum semester is 10")
    private Integer semester;

    @NotBlank(message = "Student carnet is required")
    @Pattern(regexp = "\\d{10}", message = "Carnet must have exactly 10 digits")
    @Schema(example = "2019050123")
    private String studentCarnet;

    @NotBlank(message = "Photo URL is required")
    @Schema(example = "https://storage.example.com/photos/user123.jpg")
    private String photoUrl;

    @Size(max = 200, message = "Biography cannot exceed 200 characters")
    @Schema(example = "Systems engineering student passionate about software development.")
    private String biography;

    @NotBlank(message = "Privacy level is required")
    @Schema(description = "Allowed values: PUBLIC, PRIVATE, MATCH_ONLY", example = "PUBLIC")
    private String privacyLevel;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(example = "2001-05-15")
    private LocalDate dateOfBirth;

    @NotNull(message = "Geolocation enabled is required")
    @Schema(example = "true")
    private Boolean geolocationEnabled;
}
