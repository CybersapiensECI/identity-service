package co.edu.escuelaing.alphaeci.identity_service.domain.model;

import java.time.LocalDate;

public record RegistrationProfile(
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
