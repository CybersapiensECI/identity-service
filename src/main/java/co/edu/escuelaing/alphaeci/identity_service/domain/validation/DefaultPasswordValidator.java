package co.edu.escuelaing.alphaeci.identity_service.domain.validation;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import lombok.Data;

@Data
public class DefaultPasswordValidator implements PasswordValidator {

    private static final String SPECIAL_CHARS = "!@#$,.";

    public void isValid(String value) {
        if (value == null || value.length() < 8) {
            throw new InvalidInputException(
                    "The password must be at least 8 characters long");
        }

        if (!containsUppercaseLetter(value)) {
            throw new InvalidInputException(
                    "The password must contain at least one uppercase letter");
        }

        if (!containsRequiredSpecialChar(value)) {
            throw new InvalidInputException(
                    "The password must contain at least one of the following characters: !@#$,.");
        }

    }

    private boolean containsUppercaseLetter(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (Character.isUpperCase(value.charAt(index))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsRequiredSpecialChar(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (SPECIAL_CHARS.indexOf(value.charAt(index)) >= 0) {
                return true;
            }
        }
        return false;
    }

}
