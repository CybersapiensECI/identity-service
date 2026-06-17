package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;

import java.util.Optional;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;

public interface OtpRepositoryPort {

    void save(String email, OtpEmbedded otp, OtpType type);

    Optional<OtpEmbedded> findByEmail(String email, OtpType type);

    void delete(String email, OtpType type);

}