package co.edu.escuelaing.alphaeci.identity_service.application.usecase;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ChangePasswordRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ResetPasswordRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.PasswordPort;

public class PasswordUseCase implements PasswordPort {

    @Override
    public void changePassword(ChangePasswordRequestDto dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changePassword'");
    }

    @Override
    public void forgotPassword(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'forgotPassword'");
    }

    @Override
    public void resetPassword(ResetPasswordRequestDto dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetPassword'");
    }
    
    

}
