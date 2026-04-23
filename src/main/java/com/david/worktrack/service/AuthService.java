package com.david.worktrack.service;

import com.david.worktrack.dto.AuthResponse;
import com.david.worktrack.dto.LoginRequest;
import com.david.worktrack.dto.RegisterRequest;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.exception.BusinessException;
import com.david.worktrack.refreshToken.RefreshTokenService;
import com.david.worktrack.service.token.ConfirmationToken;
import com.david.worktrack.service.token.ConfirmationTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserService appUserService;
    private final JwtService jwtService;
    private final ConfirmationTokenService confirmationTokenService;
    private final RefreshTokenService refreshTokenService;
    private final EmailServiceImp emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Registers a new appUser.
     * Protects from creating multiple users by checking if email already exists.
     */
    @Transactional
    public String register(RegisterRequest request) {

        // Check if email exists
        appUserService.checkEmailExistsOrThrow(request.getEmail());

        // Create and save user
        AppUser appUser = appUserService.createAndSaveUser(request);

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

        return "User registered. Please check your email to confirm your account";
    }

    @Transactional
    public String confirmToken(String token) {

        AppUser appUser = confirmationTokenService.markAsUsed(token);

        appUserService.enableAppUser(appUser.getEmail());

        return "Email confirmed successfully";
    }

    public AuthResponse login(LoginRequest request) {

        AppUser appUser = appUserService.getUserByEmailOrThrow(request.getEmail());

        appUserService.validatePassword(request.getPassword(), appUser.getPassword());

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

        Optional<AppUser> user = appUserService.getUserByEmail(email);

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
        appUserService.updatePassword(appUser, newPassword);
    }

    public void ensureUserIsEnable(AppUser appUser){

        if (!appUser.isEnabled()) {
            throw new BusinessException("Email not confirmed. Please confirm your email");
        }
    }

    public AppUser getCurrentUser(Authentication authentication) {

        return appUserService.getUserByEmailOrThrow(authentication.getName());
    }
}
