package com.david.worktrack.service.token;

import com.david.worktrack.dto.ConfirmationResult;
import com.david.worktrack.exception.InvalidTokenException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    // New method
    @Transactional
    public ConfirmationResult setConfirmedAt(String token) {

        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token expired");
        }

        if (confirmationToken.getConfirmedAt() != null) {
            return new ConfirmationResult(
                    "Email already confirmed!",
                    confirmationToken.getUser(),
                    false
            );
        }

        confirmationToken.setConfirmedAt(LocalDateTime.now());

        return new ConfirmationResult(
                "Email confirmed successfully",
                confirmationToken.getUser(),
                true
        );
    }
}
