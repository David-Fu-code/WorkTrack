package com.david.worktrack.controller;

import com.david.worktrack.dto.LoginRequest;
import com.david.worktrack.dto.RegisterRequest;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.repository.AppUserRepository;
import com.david.worktrack.service.AppUserService;
import com.david.worktrack.service.AuthService;
import com.david.worktrack.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;

    // Register → create ConfirmationToken → email sent → user clicks → confirms email
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(result);
    }

    // Login → generate JWT token → client stores → sends on every request
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request){
        String jwtToken = authService.login(request);
        return ResponseEntity.ok(jwtToken);
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
}
