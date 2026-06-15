package co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;

/**
 * Value object representing a validated, non-blank JWT token string.
 * Throws {@link InvalidInputException} if the value is null or blank.
 */
public record JwtToken(String value) {

    /** @throws InvalidInputException if the token value is null or blank */
    public JwtToken {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException("JWT token must not be blank");
        }
    }
}