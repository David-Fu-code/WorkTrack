package com.david.worktrack.controller;

import com.david.worktrack.dto.*;
import com.david.worktrack.refreshToken.RefreshTokenService;
import com.david.worktrack.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {

        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){

        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestParam("token") String token){

        //System.out.println("CONFIRM ENDPOINT HIT - token: " + token);
        authService.confirmToken(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test-protected")
    public ResponseEntity<String> testProtected(Authentication authentication){

        return ResponseEntity.ok("Hello " + authentication.getName() + ", this is a protected endpoint");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody AuthResponse request){

        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request){

        refreshTokenService.invalidateRefreshToken(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
