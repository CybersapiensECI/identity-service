package co.edu.escuelaing.alphaeci.identity_service.domain.model;

import java.time.LocalDateTime;

import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String id;
    private Email email;
    private String password;
    private Role role;
    private AccountStatus status;
    @Builder.Default
    private boolean verified = false;
    @Builder.Default
    private Integer failedAttempts = 0;
    private LocalDateTime blockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
