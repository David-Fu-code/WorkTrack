package com.david.worktrack.service.token;

import com.david.worktrack.dto.ConfirmationResult;
import com.david.worktrack.exception.InvalidTokenException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public ConfirmationToken getTokenOrThrow(String token) {
        return confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));
    }

    // New method
    @Transactional
    public ConfirmationResult markAsUsed(String token) {

        ConfirmationToken confirmationToken = getTokenOrThrow(token);

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token expired");
        }

        int updated = confirmationTokenRepository.confirmToken(token, LocalDateTime.now());

        if (updated == 0) {
            return new ConfirmationResult(
                    "Email already confirmed!",
                    confirmationToken.getAppUser(),
                    false
            );
        }

        return new ConfirmationResult(
                "Email confirmed successfully",
                confirmationToken.getAppUser(),
                true
        );
    }
}
