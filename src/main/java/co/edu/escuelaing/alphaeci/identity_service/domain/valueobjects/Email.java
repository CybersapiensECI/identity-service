package co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;

public class Email {

    private final String value;

    public Email(String value) {
        if (value == null) {
            throw new InvalidInputException(
                    "The email must be institutional (@escuelaing.edu.co or @mail.escuelaing.edu.co)");
        }
        String normalized = value.trim().toLowerCase();
        if (!normalized.endsWith("@escuelaing.edu.co") && !normalized.endsWith("@mail.escuelaing.edu.co")) {
            throw new InvalidInputException(
                    "The email must be institutional (@escuelaing.edu.co or @mail.escuelaing.edu.co)");
        }
        this.value = normalized;
    }

    public String getValue() {
        return value;
    }
}