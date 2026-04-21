package com.david.worktrack.passwordReset;

import com.david.worktrack.email.EmailSender;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.exception.InvalidTokenException;
import com.david.worktrack.repository.AppUserRepository;
import com.david.worktrack.service.AppUserService;
import com.david.worktrack.service.EmailServiceImp;
import com.david.worktrack.service.token.ConfirmationToken;
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

    private final PasswordResetTokenRepository resetTokenRepository;
    private final AppUserRepository appUserRepository;
    private final EmailServiceImp emailServiceImp;
    private final AppUserService appUserService;

    public void createdPasswordResetToken(String email) {

        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        // Generate token
        String token = UUID.randomUUID().toString();

        // Save token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setAppUser(appUser);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        resetTokenRepository.save(resetToken);

        // Build link
        String link = "http://localhost:8080/api/v1/auth/reset-password?token=" + token;

        // Send email
        emailServiceImp.sendResetPasswordEmail(email, appUser.getDisplayName(), link);
    }

    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = getResetTokenOrThrow(token);

        if (resetToken.isUsed()){
            throw new IllegalStateException("Token already used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("Expired token");
        }

        AppUser appUser = resetToken.getAppUser();
        appUser.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        appUserRepository.save(appUser);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }

    public PasswordResetToken getResetTokenOrThrow(String token) {

        return resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));
    }
}
