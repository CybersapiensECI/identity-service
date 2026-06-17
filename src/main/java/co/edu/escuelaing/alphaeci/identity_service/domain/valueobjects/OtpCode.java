package co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects;

import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.OtpInvalidException;

/**
 * Value object representing a validated 6-digit OTP code.
 * Rejects null values or strings that do not match exactly six decimal digits.
 */
public record OtpCode(String value) {

    private static final String SIX_DIGITS = "\\d{6}";

    /** @throws OtpInvalidException if the value is null or not exactly 6 digits */
    public OtpCode {
        if (value == null || !value.matches(SIX_DIGITS)) {
            throw new OtpInvalidException("OTP must be a 6-digit number");
        }
    }

    public static OtpCode of(String value) {
        return new OtpCode(value);
    }
}