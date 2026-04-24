package com.david.worktrack.auth;

import com.david.worktrack.auth.dto.AuthResponse;
import com.david.worktrack.auth.dto.LoginRequest;
import com.david.worktrack.auth.dto.RegisterRequest;
import com.david.worktrack.user.entity.AppUser;
import com.david.worktrack.common.exception.BusinessException;
import com.david.worktrack.token.refreshToken.RefreshTokenService;
import com.david.worktrack.user.service.UserService;
import com.david.worktrack.common.email.EmailService;
import com.david.worktrack.security.jwt.JwtService;
import com.david.worktrack.token.confirmation.ConfirmationToken;
import com.david.worktrack.token.confirmation.ConfirmationTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final ConfirmationTokenService confirmationTokenService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Registers a new appUser.
     * Protects from creating multiple users by checking if email already exists.
     */
    @Transactional
    public void register(RegisterRequest request) {

        // Check if email exists
        userService.checkEmailExistsOrThrow(request.getEmail());

        // Create and save user
        AppUser appUser = userService.createAndSaveUser(request);

        // Create and save token
        ConfirmationToken token = confirmationTokenService.createAndSaveToken(appUser);

        // Confirmation link
        String link = frontendUrl + "/confirm?token=" + token.getToken();

        // Send Email
        emailService.sendConfirmationEmail(
                appUser.getEmail(),
                appUser.getDisplayName(),
                link
        );
    }

    @Transactional
    public void confirmToken(String token) {

        AppUser appUser = confirmationTokenService.markAsUsed(token);

        userService.enableAppUser(appUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {

        AppUser appUser = userService.getUserByEmailOrThrow(request.getEmail());

        userService.validatePassword(request.getPassword(), appUser.getPassword());

        ensureUserIsEnable(appUser);

        // Generate JWT token & Refresh Token
        String accessToken  = jwtService.generateToken(appUser.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(appUser);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshTokenValue) {

        AppUser appUser =  refreshTokenService.validateRefreshToken(refreshTokenValue);

        String newAccessToken = jwtService.generateToken(appUser.getEmail());

        return new AuthResponse(newAccessToken, refreshTokenValue);
    }

    // Security measure to avoid user enumeration (information disclosure)
    // The response is intentionally the same whether the email exists or not,
    // to prevent attackers from discovering valid accounts
    public void forgotPassword(String email) {

        Optional<AppUser> user = userService.getUserByEmail(email);

        user.ifPresent(appUser -> {
            // Create reset token
            ConfirmationToken token = confirmationTokenService.createAndSaveToken(appUser);

            String link = frontendUrl + "/reset-password?token=" + token.getToken();

            // Send RESET password email
            emailService.sendResetPasswordEmail(
                    appUser.getEmail(),
                    appUser.getDisplayName(),
                    link
            );
        });

    }

    @Transactional
    public void resetPassword(String resetPasswordToken, String newPassword) {

        // Mark resetPasswordToken as used
        AppUser appUser = confirmationTokenService.markAsUsed(resetPasswordToken);

        // Update password
        userService.updatePassword(appUser, newPassword);
    }

    public void ensureUserIsEnable(AppUser appUser){

        if (!appUser.isEnabled()) {
            throw new BusinessException("Email not confirmed. Please confirm your email");
        }
    }

    public AppUser getCurrentUser(Authentication authentication) {

        return userService.getUserByEmailOrThrow(authentication.getName());
    }
}
