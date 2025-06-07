package com.david.worktrack.passwordReset;

import com.david.worktrack.email.EmailSender;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final AppUserRepository appUserRepository;
    private final EmailSender emailSender;

    public void createdPasswordResetToken(String email) {

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Generate token
        String token = UUID.randomUUID().toString();

        // Save token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(appUser);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        // Build link
        String link = "http://localhost:8080/api/v1/auth/confirm?token=" + token;

        // Send email
        emailSender.send(appUser.getEmail(), buildEmail(link));
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid token"));

        if (resetToken.isUsed()){
            throw new IllegalStateException("Token already used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("Expired token");
        }

        AppUser appuser = resetToken.getUser();
        appuser.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        appUserRepository.save(appuser);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private String buildEmail(String link) {
        return "Click the following link to reset your password: " + link;
    }
}
