package co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpInvalidException;

class OtpCodeTest {

    @Test
    void otpCode_validSixDigits_createsSuccessfully() {
        OtpCode code = new OtpCode("123456");
        assertThat(code.value()).isEqualTo("123456");
    }

    @Test
    void otpCode_ofFactory_returnsSameResult() {
        OtpCode code = OtpCode.of("654321");
        assertThat(code.value()).isEqualTo("654321");
    }

    @Test
    void otpCode_allZeros_isValid() {
        OtpCode code = new OtpCode("000000");
        assertThat(code.value()).isEqualTo("000000");
    }

    @Test
    void otpCode_null_throwsOtpInvalid() {
        assertThatThrownBy(() -> new OtpCode(null))
                .isInstanceOf(OtpInvalidException.class);
    }

    @Test
    void otpCode_fiveDigits_throwsOtpInvalid() {
        assertThatThrownBy(() -> new OtpCode("12345"))
                .isInstanceOf(OtpInvalidException.class);
    }

    @Test
    void otpCode_sevenDigits_throwsOtpInvalid() {
        assertThatThrownBy(() -> new OtpCode("1234567"))
                .isInstanceOf(OtpInvalidException.class);
    }

    @Test
    void otpCode_nonNumeric_throwsOtpInvalid() {
        assertThatThrownBy(() -> new OtpCode("abcdef"))
                .isInstanceOf(OtpInvalidException.class);
    }

    @Test
    void otpCode_mixedAlphaNumeric_throwsOtpInvalid() {
        assertThatThrownBy(() -> new OtpCode("12a456"))
                .isInstanceOf(OtpInvalidException.class);
    }

    @Test
    void otpCode_recordEquality_sameValueAreEqual() {
        OtpCode a = new OtpCode("111111");
        OtpCode b = OtpCode.of("111111");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void otpCode_recordEquality_differentValuesAreNotEqual() {
        OtpCode a = new OtpCode("111111");
        OtpCode b = new OtpCode("222222");
        assertThat(a).isNotEqualTo(b);
    }
}
