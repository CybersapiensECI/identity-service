package co.edu.escuelaing.alphaeci.identity_service.domain.ports.in;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.VerificationRequestDto;

public interface VerificationPort {
    void initVerification(VerificationRequestDto dto);
}
