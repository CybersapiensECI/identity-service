package co.edu.escuelaing.alphaeci.identity_service.domain.validation;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;

class DefaultPasswordValidatorTest {

    private final DefaultPasswordValidator validator = new DefaultPasswordValidator();

    @Test
    void isValid_validPassword_noException() {
        assertThatNoException().isThrownBy(() -> validator.isValid("SecurePass1!"));
    }

    @Test
    void isValid_null_throwsInvalidInput() {
        assertThatThrownBy(() -> validator.isValid(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("at least 8 characters");
    }

    @Test
    void isValid_tooShort_throwsInvalidInput() {
        assertThatThrownBy(() -> validator.isValid("Ab1!"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("at least 8 characters");
    }

    @Test
    void isValid_noUppercase_throwsInvalidInput() {
        assertThatThrownBy(() -> validator.isValid("lowercase1!"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("uppercase letter");
    }

    @Test
    void isValid_noSpecialChar_throwsInvalidInput() {
        assertThatThrownBy(() -> validator.isValid("NoSpecialChar1"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("!@#$,.");
    }

    @Test
    void isValid_allSpecialCharsAccepted() {
        assertThatNoException().isThrownBy(() -> validator.isValid("Password@1"));
        assertThatNoException().isThrownBy(() -> validator.isValid("Password#1"));
        assertThatNoException().isThrownBy(() -> validator.isValid("Password,1"));
        assertThatNoException().isThrownBy(() -> validator.isValid("Password.1"));
    }
}
