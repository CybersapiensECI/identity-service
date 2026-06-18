package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.mapper;

import org.mapstruct.Mapper;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.CompleteRegistrationRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.RegistrationProfile;

@Mapper(componentModel = "spring")
public interface RegistrationProfileMapper {
    RegistrationProfile toRegistrationProfile(CompleteRegistrationRequestDto dto);
}
