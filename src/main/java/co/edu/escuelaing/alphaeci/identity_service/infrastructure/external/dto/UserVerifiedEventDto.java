package co.edu.escuelaing.alphaeci.identity_service.infrastructure.external.dto;

import java.time.LocalDate;

public record UserVerifiedEventDto(
        String userId,
        String email,
        String role,
        String name,
        String gender,
        String career,
        Integer semester,
        String studentCarnet,
        String photoUrl,
        String biography,
        String privacyLevel,
        LocalDate dateOfBirth,
        Boolean geolocationEnabled) {
}
