package com.david.worktrack.controller;

import com.david.worktrack.dto.*;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getAppUserRole());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateProfileRequest request, Authentication authentication){
        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setDisplayName(request.getName());
        appUserRepository.save(user);

        return ResponseEntity.ok("User updated successfully");
    }

    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication){
        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!bCryptPasswordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
        }

        user.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }
}
