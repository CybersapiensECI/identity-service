package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.AccountStatus;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.Role;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.User;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.entity.UserEntity;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.mapper.UserMapper;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.repository.UserJpaRepository;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock private UserJpaRepository repository;
    @Mock private UserMapper mapper;

    @InjectMocks private UserRepositoryAdapter adapter;

    private User sampleUser() {
        return User.builder()
                .id("uid-1")
                .email(new Email("user@mail.escuelaing.edu.co"))
                .password("$2a$10$hash")
                .role(Role.STUDENT)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    private UserEntity sampleEntity() {
        return UserEntity.builder()
                .id("uid-1")
                .email("user@mail.escuelaing.edu.co")
                .passwordHash("$2a$10$hash")
                .role(Role.STUDENT)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void save_mapsAndPersistsUser() {
        User user = sampleUser();
        UserEntity entity = sampleEntity();
        when(mapper.toEntity(user)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(user);

        User result = adapter.save(user);

        assertThat(result).isEqualTo(user);
        verify(repository).save(entity);
    }

    @Test
    void findById_found_returnsMappedUser() {
        UserEntity entity = sampleEntity();
        User user = sampleUser();
        when(repository.findById("uid-1")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = adapter.findById("uid-1");

        assertThat(result).contains(user);
    }

    @Test
    void findById_notFound_returnsEmpty() {
        when(repository.findById("uid-1")).thenReturn(Optional.empty());

        assertThat(adapter.findById("uid-1")).isEmpty();
    }

    @Test
    void findByEmail_found_returnsMappedUser() {
        String email = "user@mail.escuelaing.edu.co";
        UserEntity entity = sampleEntity();
        User user = sampleUser();
        when(repository.findByEmail(email)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = adapter.findByEmail(email);

        assertThat(result).contains(user);
    }

    @Test
    void findByEmail_notFound_returnsEmpty() {
        when(repository.findByEmail("unknown@mail.escuelaing.edu.co")).thenReturn(Optional.empty());

        assertThat(adapter.findByEmail("unknown@mail.escuelaing.edu.co")).isEmpty();
    }

    @Test
    void update_savesEntityFromDomain() {
        User user = sampleUser();
        UserEntity entity = sampleEntity();
        when(mapper.toEntity(user)).thenReturn(entity);

        adapter.update(user);

        verify(repository).save(entity);
    }
}
