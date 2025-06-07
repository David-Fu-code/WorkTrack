package com.david.worktrack.service;

import com.david.worktrack.dto.AuthResponse;
import com.david.worktrack.dto.LoginRequest;
import com.david.worktrack.dto.RegisterRequest;
import com.david.worktrack.email.EmailSender;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.entity.AppUserRole;
import com.david.worktrack.refreshToken.RefreshToken;
import com.david.worktrack.refreshToken.RefreshTokenService;
import com.david.worktrack.repository.AppUserRepository;
import com.david.worktrack.service.token.ConfirmationToken;
import com.david.worktrack.service.token.ConfirmationTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserService appUserService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AppUserRepository appUserRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final JwtService jwtService;
    private final EmailSender emailSender;
    private final RefreshTokenService refreshTokenService;
    private AuthResponse authResponse;

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
        AppUser appUser = AppUser.builder()
                .email(request.getEmail()) // Sets email
                .password(bCryptPasswordEncoder.encode(request.getPassword())) // Encodes password
                .appUserRole(AppUserRole.USER) // Sets role to USER (default role)
                .displayName(request.getDisplayName())
                .enabled(false)  // Needs email confirm
                .locked(false) // Account is not blocked
                .verified(false) // Needs email confirm
                .build();

        // Save user
        appUserRepository.save(appUser);

        // Create ConfirmationToken
        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .appUser(appUser)
                .build();

        // Save token
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        // Confirmation link
        String link = "http://localhost:8080/api/v1/auth/confirm?token=" + token;

        // Send Email
        emailSender.send(request.getEmail(), buildEmail(request.getEmail(), link));

        return "User registered. Please chek your email to confirm";
    }

    @Transactional
    public String confirmToken(String token) {

        // Find token
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        // Check confirmed
        if(confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Email already confirmed");
        }

        // Check if token expired
        if (confirmationToken.getExpiredAt().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("Token expired");
        }

        // Token confirmed
        confirmationTokenService.setConfirmedAt(token);
        // Enable User
        appUserService.enableAppUser(confirmationToken.getAppUser().getEmail());
        return "Confirmed";

    }

    public AuthResponse login(LoginRequest request) {
        // Load User
        AppUser user = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        // Check password
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalStateException("Incorrect password");
        }

        // Check if User enable or verified
        if(!user.isEnabled() || !user.isVerified()){
            throw new IllegalStateException("Email not confirmed. Please confirm your email.");
        }

        // Generate JWT token & Refresh Token
        String accessToken  = jwtService.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }

    private String buildEmail(String name, String link) {
        return "<p>Hello " + name + ",</p>"
                + "<p>Thank you for registering. Please click on the below link to activate your account:</p>"
                + "<a href=\"" + link + "\">Confirm Account</a>"
                + "<p>The link will expire in 15 minutes.</p>"
                + "<p>See you soon!</p>";
    }

    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);

        String newAccessToken = jwtService.generateToken(refreshToken.getAppUser().getEmail());

        return new AuthResponse(newAccessToken, refreshTokenValue);
    }

    public void forgotPassword(String email) {

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Generate token
        String token = UUID.randomUUID().toString();

        // Save token en la tabla de confirmation tokens (puedes usar la misma tabla que confirm email)
        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .appUser(user)
                .build();

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        String link = "http://localhost:8080/api/v1/auth/reset-password?token=" + token;


        emailSender.send(email, buildForgotPasswordEmail(email, link));
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find token
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid or expired token"));

        // Check if token not expired
        if (confirmationToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expired");
        }

        // Token = Used
        confirmationTokenService.setConfirmedAt(token);

        // Update password
        AppUser user = confirmationToken.getAppUser();
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));

        appUserRepository.save(user);
    }

    private String buildForgotPasswordEmail(String name, String link) {
        return "<p>Hello " + name + ",</p>"
                + "<p>You requested to reset your password. Please click on the link below to reset it:</p>"
                + "<a href=\"" + link + "\">" + link + "</a>"  // Show full link
                + "<p>This link will expire in 15 minutes.</p>"
                + "<p>If you did not request this, you can ignore this email.</p>";
    }


}
