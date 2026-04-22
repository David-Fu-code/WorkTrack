package com.david.worktrack.service.token;

import com.david.worktrack.dto.ConfirmationResult;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.exception.BusinessException;
import com.david.worktrack.exception.InvalidTokenException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.rmi.ConnectIOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationToken getTokenOrThrow(String token) {

        return confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));
    }

    @Transactional
    public AppUser markAsUsed(String token) {

        ConfirmationToken confirmationToken = validateToken(token);

        int updated = confirmationTokenRepository.confirmToken(token, LocalDateTime.now());

        if (updated == 0) {
            throw new BusinessException("Email already confirmed");
        }

        return confirmationToken.getAppUser();

    }

    public ConfirmationToken validateToken(String token) {

        ConfirmationToken confirmationToken = getTokenOrThrow(token);

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token expired");
        }

        return confirmationToken;
    }

    public ConfirmationToken createAndSaveToken(AppUser appUser) {

        LocalDateTime now = LocalDateTime.now();

        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(UUID.randomUUID().toString())
                .createdAt(now)
                .expiresAt(now.plusMinutes(15))
                .appUser(appUser)
                .build();

        return confirmationTokenRepository.save(confirmationToken);
    }
}
