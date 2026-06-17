package co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;

class EmailTest {

    @Test
    void email_validEscuelaingDomain_createsSuccessfully() {
        Email email = new Email("student@escuelaing.edu.co");
        assertThat(email.getValue()).isEqualTo("student@escuelaing.edu.co");
    }

    @Test
    void email_validMailEscuelaingDomain_createsSuccessfully() {
        Email email = new Email("student@mail.escuelaing.edu.co");
        assertThat(email.getValue()).isEqualTo("student@mail.escuelaing.edu.co");
    }

    @Test
    void email_uppercaseInput_normalizesToLowercase() {
        Email email = new Email("STUDENT@ESCUELAING.EDU.CO");
        assertThat(email.getValue()).isEqualTo("student@escuelaing.edu.co");
    }

    @Test
    void email_inputWithSpaces_isTrimmedAndLowercased() {
        Email email = new Email("  Student@Escuelaing.Edu.Co  ");
        assertThat(email.getValue()).isEqualTo("student@escuelaing.edu.co");
    }

    @Test
    void email_null_throwsInvalidInput() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void email_externalDomain_throwsInvalidInput() {
        assertThatThrownBy(() -> new Email("user@gmail.com"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void email_partialDomainMatch_throwsInvalidInput() {
        assertThatThrownBy(() -> new Email("user@fake-escuelaing.edu.co"))
                .isInstanceOf(InvalidInputException.class);
    }
}
