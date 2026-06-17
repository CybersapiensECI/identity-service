package co.edu.escuelaing.alphaeci.identity_service.application.dto.external;

import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String passwordHash,
        boolean verified,
        String userType) {
}
