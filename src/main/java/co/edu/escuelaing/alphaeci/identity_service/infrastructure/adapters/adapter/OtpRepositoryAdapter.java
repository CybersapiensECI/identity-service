package co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.escuelaing.alphaeci.identity_service.domain.model.OtpType;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.out.OtpRepositoryPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpCode;
import co.edu.escuelaing.alphaeci.identity_service.domain.valueobjects.OtpEmbedded;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.OtpCache;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.entity.PasswordResetOtpCache;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository.OtpRedisRepository;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.cache.repository.PasswordResetOtpRedisRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OtpRepositoryAdapter implements OtpRepositoryPort {

    private final OtpRedisRepository otpRedisRepository;
    private final PasswordResetOtpRedisRepository passwordResetOtpRedisRepository;

    @Override
    public void save(String email, OtpEmbedded otp, OtpType type) {
        if (type == OtpType.EMAIL_VERIFICATION) {
            otpRedisRepository.save(OtpCache.builder()
                    .email(email)
                    .code(otp.getCode().value())
                    .used(Boolean.TRUE.equals(otp.getUsed()))
                    .attempts(otp.getAttempts() != null ? otp.getAttempts() : 0)
                    .build());
        } else {
            passwordResetOtpRedisRepository.save(PasswordResetOtpCache.builder()
                    .email(email)
                    .code(otp.getCode().value())
                    .used(Boolean.TRUE.equals(otp.getUsed()))
                    .attempts(otp.getAttempts() != null ? otp.getAttempts() : 0)
                    .build());
        }
    }

    @Override
    public Optional<OtpEmbedded> findByEmail(String email, OtpType type) {
        if (type == OtpType.EMAIL_VERIFICATION) {
            return otpRedisRepository.findById(email).map(this::fromOtpCache);
        }
        return passwordResetOtpRedisRepository.findById(email).map(this::fromPasswordResetCache);
    }

    @Override
    public void delete(String email, OtpType type) {
        if (type == OtpType.EMAIL_VERIFICATION) {
            otpRedisRepository.deleteById(email);
        } else {
            passwordResetOtpRedisRepository.deleteById(email);
        }
    }

    private OtpEmbedded fromOtpCache(OtpCache cache) {
        OtpEmbedded otp = new OtpEmbedded();
        otp.setCode(new OtpCode(cache.getCode()));
        otp.setExpiresAt(null); // expiry delegated to Redis TTL
        otp.setUsed(cache.isUsed());
        otp.setAttempts(cache.getAttempts());
        return otp;
    }

    private OtpEmbedded fromPasswordResetCache(PasswordResetOtpCache cache) {
        OtpEmbedded otp = new OtpEmbedded();
        otp.setCode(new OtpCode(cache.getCode()));
        otp.setExpiresAt(null); // expiry delegated to Redis TTL
        otp.setUsed(cache.isUsed());
        otp.setAttempts(cache.getAttempts());
        return otp;
    }
}
