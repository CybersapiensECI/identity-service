package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.entity.UserEntity;

@Mapper(componentModel = "spring", imports = { Email.class })
public interface UserMapper {

    @Mapping(target = "email",    expression = "java(new Email(entity.getEmail()))")
    User toDomain(UserEntity entity);

    @Mapping(target = "email",        expression = "java(user.getEmail().getValue())")
    UserEntity toEntity(User user);
}
