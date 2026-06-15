package edu.eci.patricia.DOSW_patricia.domain.valueobjects;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Value object encapsulating an OTP code together with its expiry, usage state,
 * and attempt counter. Used both for registration OTPs and password-reset codes.
 */
@Data
public class OtpEmbedded {

    private static final int MAX_ATTEMPTS = 3;

    private String codigo;
    private LocalDateTime expiraEn;
    private Boolean usado;
    private Integer intentos;


    /** @return {@code true} if the OTP has not been used and has not yet expired */
    public boolean esValido() {
        return !Boolean.TRUE.equals(usado) && expiraEn.isAfter(LocalDateTime.now());
    }

    /** @return {@code true} if the expiry timestamp is in the past */
    public boolean haExpirado() {
        return expiraEn.isBefore(LocalDateTime.now());
    }

    /** Marks this OTP as consumed so it cannot be reused. */
    public void marcaUsado() {
        this.usado = true;
    }

    /** Increments the failed-attempt counter by one. */
    public void incrementarIntentos() {
        this.intentos = (this.intentos == null ? 0 : this.intentos) + 1;
    }

    /** @return {@code true} if the attempt count has reached or exceeded {@value MAX_ATTEMPTS} */
    public boolean haAlcanzadoLimite() {
        return intentos != null && intentos >= MAX_ATTEMPTS;
    }
}