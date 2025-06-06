package com.david.worktrack.service;

import com.david.worktrack.dto.LoginRequest;
import com.david.worktrack.dto.RegisterRequest;
import com.david.worktrack.email.EmailSender;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.entity.AppUserRole;
import com.david.worktrack.repository.AppUserRepository;
import com.david.worktrack.service.token.ConfirmationToken;
import com.david.worktrack.service.token.ConfirmationTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    public String login(LoginRequest request) {
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

        // Generate JWT token
        return jwtService.generateToken(user.getEmail());
    }

    private String buildEmail(String name, String link) {
        return "<p>Hello " + name + ",</p>"
                + "<p>Thank you for registering. Please click on the below link to activate your account:</p>"
                + "<a href=\"" + link + "\">Confirm Account</a>"
                + "<p>The link will expire in 15 minutes.</p>"
                + "<p>See you soon!</p>";
    }
}
