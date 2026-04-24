package com.david.worktrack.token.passwordReset;
import com.david.worktrack.user.entity.AppUser;
import com.david.worktrack.common.exception.InvalidTokenException;
import com.david.worktrack.user.service.UserService;
import com.david.worktrack.common.email.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    private final UserService userService;

    @Value("${app.backend.url}")
    private String backendUrl;

    public void createPasswordResetToken(String email) {

        AppUser appUser = userService.getUserByEmailOrThrow(email);

        // Generate token
        String token = UUID.randomUUID().toString();

        // Save token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setAppUser(appUser);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        resetTokenRepository.save(resetToken);

        String link = backendUrl + "/api/v1/auth/reset-password?token=" + token;

        emailService.sendResetPasswordEmail(email, appUser.getDisplayName(), link);
    }

    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = getResetTokenOrThrow(token);

        if (resetToken.isUsed()){
            throw new InvalidTokenException("Token already used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new InvalidTokenException("Expired token");
        }

        AppUser appUser = resetToken.getAppUser();
        userService.updatePassword(appUser, newPassword);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }

    public PasswordResetToken getResetTokenOrThrow(String token) {

        return resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));
    }
}
