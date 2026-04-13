package com.david.worktrack.service.token;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import com.david.worktrack.entity.AppUser;

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
                .orElseThrow(() -> new IllegalStateException("❌ Invalid or expired confirmation token"));

        String message;
        boolean firstTime = false;

        if (confirmationToken.getConfirmedAt() != null) {
            message = "✅ Email already confirmed!";
        } else if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("❌ Invalid or expired confirmation token");
        } else {
            confirmationToken.setConfirmedAt(LocalDateTime.now());
            confirmationTokenRepository.save(confirmationToken);
            message = "✅ Email confirmed successfully!";
            firstTime = true;
        }

        return new ConfirmationResult(message, confirmationToken.getAppUser(), firstTime);
    }

    // DTO simple to return message + user
    public static record ConfirmationResult(String message, AppUser user, boolean firstTime) {}
}
