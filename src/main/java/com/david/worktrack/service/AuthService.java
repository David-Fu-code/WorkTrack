package com.david.worktrack.service;

import com.david.worktrack.dto.AuthResponse;
import com.david.worktrack.dto.ConfirmationResult;
import com.david.worktrack.dto.LoginRequest;
import com.david.worktrack.dto.RegisterRequest;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.entity.AppUserRole;
import com.david.worktrack.exception.BusinessException;
import com.david.worktrack.exception.InvalidTokenException;
import com.david.worktrack.refreshToken.RefreshToken;
import com.david.worktrack.refreshToken.RefreshTokenService;
import com.david.worktrack.repository.AppUserRepository;
import com.david.worktrack.service.token.ConfirmationToken;
import com.david.worktrack.service.token.ConfirmationTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserService appUserService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final ConfirmationTokenService confirmationTokenService;
    private final RefreshTokenService refreshTokenService;
    private final EmailServiceImp emailService;
    private final TokenService customTokenService;
    /**
     * Registers a new appUser.
     * Protects from creating multiple users by checking if email already exists.
     */
    public String register(RegisterRequest request) {

        // Check if email exists
        if (appUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }
        // Build new appUser
        AppUser appUser = AppUser.builder()
                .email(request.getEmail()) // Sets email
                .password(bCryptPasswordEncoder.encode(request.getPassword())) // Encodes password
                .appUserRole(AppUserRole.USER) // Sets role to USER (default role)
                .displayName(request.getDisplayName())
                .enabled(false)  // Needs email confirm
                .locked(false) // Account is not blocked
                .verified(false) // Needs email confirm
                .build();

        // Save appUser
        appUserRepository.save(appUser);

        ConfirmationToken token = customTokenService.createToken(appUser);
        confirmationTokenService.saveConfirmationToken(token);

        // Confirmation link
        String link = "http://localhost:3000/confirm?token=" + token.getToken();

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

        ConfirmationResult result = confirmationTokenService.markAsUsed(token);

        if (result.firstTime()) {
            appUserService.enableAppUser(result.user().getEmail());
        }

        return result.message();
    }

    public AuthResponse login(LoginRequest request) {

        AppUser appUser = appUserService.getUserByEmailOrThrow(request.getEmail());

        if (!bCryptPasswordEncoder.matches(request.getPassword(), appUser.getPassword())) {
            throw new BusinessException("Incorrect password");
        }

        if(!appUser.isEnabled() || !appUser.isVerified()){
            throw new BusinessException("Email not confirmed. Please confirm your email.");
        }

        // Generate JWT token & Refresh Token
        String accessToken  = jwtService.generateToken(appUser.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(appUser);
        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);

        String newAccessToken = jwtService.generateToken(refreshToken.getAppUser().getEmail());

        return new AuthResponse(newAccessToken, refreshTokenValue);
    }

    public void forgotPassword(String email) {

        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        // Create reset token
        ConfirmationToken token = customTokenService.createToken(appUser);
        confirmationTokenService.saveConfirmationToken(token);

        String link = "http://localhost:8080/api/v1/auth/reset-password?token=" + token.getToken();

        // Send RESET password email
        emailService.sendResetPasswordEmail(
                appUser.getEmail(),
                appUser.getDisplayName(),
                link
        );
    }

    @Transactional
    public void resetPassword(String resetPasswordToken, String newPassword) {

        ConfirmationToken confirmationToken = confirmationTokenService.getTokenOrThrow(resetPasswordToken);

        // Check expiration
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token expired");
        }

        // Mark resetPasswordToken as used
        confirmationTokenService.markAsUsed(resetPasswordToken);

        // Update password
        AppUser appUser = confirmationToken.getAppUser();
        appUser.setPassword(bCryptPasswordEncoder.encode(newPassword));

        appUserRepository.save(appUser);
    }
}
