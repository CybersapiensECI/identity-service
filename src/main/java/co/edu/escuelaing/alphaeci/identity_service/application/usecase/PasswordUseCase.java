package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import org.springframework.stereotype.Service;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ChangePasswordRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ResetPasswordRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.PasswordPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.EmailSenderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.PasswordEncoderPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordUseCase implements PasswordPort {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final OtpRepositoryPort otpRepository;
    private final EmailSenderPort emailSender;

    @Override
    public void changePassword(ChangePasswordRequestDto dto) {
        throw new UnsupportedOperationException("Unimplemented method 'changePassword'");
    }

    @Override
    public void forgotPassword(String email) {
        throw new UnsupportedOperationException("Unimplemented method 'forgotPassword'");
    }

    @Override
    public void resetPassword(ResetPasswordRequestDto dto) {
        throw new UnsupportedOperationException("Unimplemented method 'resetPassword'");
    }
}
