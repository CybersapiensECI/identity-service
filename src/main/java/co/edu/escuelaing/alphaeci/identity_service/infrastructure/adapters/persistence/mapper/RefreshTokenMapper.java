package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.mapper;

import org.mapstruct.Mapper;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.RefreshToken;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.entity.RefreshTokenEntity;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    RefreshToken toDomain(RefreshTokenEntity entity);

    RefreshTokenEntity toEntity(RefreshToken domain);
}
