package co.edu.escuelaing.alphaeci.identity_service.domain.model;

import java.time.LocalDateTime;

import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.Email;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.PasswordHash;
import lombok.Data;

@Data
public class User {
    private String id;

    private Email email;
    private PasswordHash password;
    private Role role;
    private AccountStatus status;
    private boolean verified;
    private Integer failedAttempts;
    private LocalDateTime blockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
