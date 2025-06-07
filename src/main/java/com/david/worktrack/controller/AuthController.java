package com.david.worktrack.controller;

import com.david.worktrack.dto.*;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.refreshToken.RefreshTokenService;
import com.david.worktrack.repository.AppUserRepository;
import com.david.worktrack.service.AppUserService;
import com.david.worktrack.service.AuthService;
import com.david.worktrack.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    // Register → create ConfirmationToken → email sent → user clicks → confirms email
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(result);
    }

    // Login → generate JWT token → client stores → sends on every request
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        AuthResponse tokens = authService.login(request);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam("token") String token){
        try {
            String result = authService.confirmToken(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/test-protected")
    public ResponseEntity<String> testProtected(Authentication authentication){
        return ResponseEntity.ok("Hello " + authentication.getName() + ", this is a protected endpoint");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody AuthResponse request){
        AuthResponse tokens = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest request){
        refreshTokenService.invalidateRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getAppUserRole());
        return ResponseEntity.ok(response);
    }

    // Update DisplayName
    @PutMapping("/users/me")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateProfileRequest request, Authentication authentication){
        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Update fields
        user.setDisplayName(request.getName());
        appUserRepository.save(user);

        return ResponseEntity.ok("User updated successfully");
    }

    // Update Password
    @PutMapping("/users/me/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication){
        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Verify current password
        if (!bCryptPasswordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
        }

        // Set new password
        user.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    // Add ADMIN endpoint
    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminTest(){
        return ResponseEntity.ok("Admin test successfully");
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        // loads all rows from users table -> as 'AppUser' objects
        List<AppUser> users = appUserRepository.findAll();

        // Convert each 'AppUser' -> into a 'UserResponse' DTO
        List<UserResponse> response = users.stream() // Loop over all users || map -> for each user, create a 'UserResponse'
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getAppUserRole()
                ))
                .toList();
        return ResponseEntity.ok(response); // Return list of user response objects

    }
}
