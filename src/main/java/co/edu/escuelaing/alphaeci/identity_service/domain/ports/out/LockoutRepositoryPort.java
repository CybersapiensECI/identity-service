package co.edu.escuelaing.alphaeci.identity_service.domain.ports.out;

public interface LockoutRepositoryPort {

    int findFailedAttempts(String email);

    void incrementFailedAttempts(String email);

    void clearFailedAttempts(String email);
}
