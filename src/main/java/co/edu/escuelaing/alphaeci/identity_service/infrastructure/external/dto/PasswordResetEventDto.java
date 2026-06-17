package co.edu.escuelaing.alphaeci.identity_service.infrastructure.external.dto;

public record PasswordResetEventDto(String email, String code) {
}
