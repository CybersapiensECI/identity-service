package co.edu.escuelaing.alphaeci.identity_service.domain.ports.in;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ValidateOtpRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.response.LoginResponseDto;

public interface OtpPort {
    void resendOtp(String email);
    LoginResponseDto validateOtp(ValidateOtpRequestDto request);
}
