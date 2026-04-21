package com.david.worktrack.service;

import com.david.worktrack.dto.AuthResponse;
import com.david.worktrack.dto.ConfirmationResult;
import com.david.worktrack.dto.LoginRequest;
import com.david.worktrack.dto.RegisterRequest;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.entity.AppUserRole;
import com.david.worktrack.exception.BusinessException;
import com.david.worktrack.exception.InvalidTokenException;
import com.david.worktrack.exception.ResourceNotFoundException;
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
    private final ConfirmationTokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final EmailServiceImp emailService;
    private final TokenService customTokenService;
    /**
     * Registers a new user.
     * Protects from creating multiple users by checking if email already exists.
     */
    public String register(RegisterRequest request) {

        // Check if email exists
        if (appUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }
        // Build new user
        AppUser user = AppUser.builder()
                .email(request.getEmail()) // Sets email
                .password(bCryptPasswordEncoder.encode(request.getPassword())) // Encodes password
                .appUserRole(AppUserRole.USER) // Sets role to USER (default role)
                .displayName(request.getDisplayName())
                .enabled(false)  // Needs email confirm
                .locked(false) // Account is not blocked
                .verified(false) // Needs email confirm
                .build();

        // Save user
        appUserRepository.save(user);

        ConfirmationToken token = customTokenService.createToken(user);
        tokenService.saveConfirmationToken(token);

        // Confirmation link
        String link = "http://localhost:3000/confirm?token=" + token.getToken();

        // Send Email
        emailService.sendConfirmationEmail(
                user.getEmail(),
                user.getDisplayName(),
                link
        );

        return "User registered. Please check your email to confirm your account";
    }

    @Transactional
    public String confirmToken(String token) {

        ConfirmationResult result = tokenService.setConfirmedAt(token);

        if (result.firstTime()) {
            appUserService.enableAppUser(result.user().getEmail());
        }

        return result.message();
    }

    public AuthResponse login(LoginRequest request) {

        AppUser user = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Incorrect password");
        }

        if(!user.isEnabled() || !user.isVerified()){
            throw new BusinessException("Email not confirmed. Please confirm your email.");
        }

        // Generate JWT token & Refresh Token
        String accessToken  = jwtService.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);

        String newAccessToken = jwtService.generateToken(refreshToken.getAppUser().getEmail());

        return new AuthResponse(newAccessToken, refreshTokenValue);
    }

    public void forgotPassword(String email) {

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create reset token
        ConfirmationToken token = customTokenService.createToken(user);
        tokenService.saveConfirmationToken(token);

        String link = "http://localhost:8080/api/v1/auth/reset-password?token=" + token.getToken();

        // Send RESET password email
        emailService.sendResetPasswordEmail(
                user.getEmail(),
                user.getDisplayName(),
                link
        );
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {

        ConfirmationToken confirmationToken = tokenService.getToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));

        // Check expiration
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token expired");
        }

        // Mark token as used
        tokenService.setConfirmedAt(token);

        // Update password
        AppUser user = confirmationToken.getUser();
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));

        appUserRepository.save(user);
    }
}
