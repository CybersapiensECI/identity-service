package co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;

class PasswordHashTest {

    @Test
    void passwordHash_validPassword_createsSuccessfully() {
        PasswordHash hash = new PasswordHash("ValidPass1!");
        assertThat(hash.getValue()).isEqualTo("ValidPass1!");
    }

    @Test
    void passwordHash_allSpecialCharsAccepted() {
        assertThat(new PasswordHash("SecureA1!").getValue()).isNotNull();
        assertThat(new PasswordHash("SecureA1@").getValue()).isNotNull();
        assertThat(new PasswordHash("SecureA1#").getValue()).isNotNull();
        assertThat(new PasswordHash("SecureA1$").getValue()).isNotNull();
        assertThat(new PasswordHash("SecureA1,").getValue()).isNotNull();
        assertThat(new PasswordHash("SecureA1.").getValue()).isNotNull();
    }

    @Test
    void passwordHash_null_throwsInvalidInput() {
        assertThatThrownBy(() -> new PasswordHash(null))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void passwordHash_tooShort_throwsInvalidInput() {
        assertThatThrownBy(() -> new PasswordHash("Sh0rt!"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void passwordHash_exactlyEightChars_isValid() {
        PasswordHash hash = new PasswordHash("ValidA1!");
        assertThat(hash.getValue()).isEqualTo("ValidA1!");
    }

    @Test
    void passwordHash_noUppercase_throwsInvalidInput() {
        assertThatThrownBy(() -> new PasswordHash("nouppercase1!"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void passwordHash_noSpecialChar_throwsInvalidInput() {
        assertThatThrownBy(() -> new PasswordHash("NoSpecial1"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void fromEncoded_validHash_bypassesValidation() {
        String bcrypt = "$2a$10$someHashValueThatIsLongEnough";
        PasswordHash hash = PasswordHash.fromEncoded(bcrypt);
        assertThat(hash.getValue()).isEqualTo(bcrypt);
    }

    @Test
    void fromEncoded_null_throwsInvalidInput() {
        assertThatThrownBy(() -> PasswordHash.fromEncoded(null))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void fromEncoded_blank_throwsInvalidInput() {
        assertThatThrownBy(() -> PasswordHash.fromEncoded("   "))
                .isInstanceOf(InvalidInputException.class);
    }
}
