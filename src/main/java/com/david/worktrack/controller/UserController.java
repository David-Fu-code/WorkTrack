package com.david.worktrack.controller;

import com.david.worktrack.dto.*;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.exception.ResourceNotFoundException;
import com.david.worktrack.repository.AppUserRepository;
import com.david.worktrack.service.AppUserService;
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
    private final AppUserService appUserService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        UserResponse response = new UserResponse(appUser.getId(), appUser.getEmail(), appUser.getDisplayName(), appUser.getAppUserRole());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateProfileRequest request, Authentication authentication){
        String email = authentication.getName();
        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        appUser.setDisplayName(request.getName());
        appUserRepository.save(appUser);

        return ResponseEntity.ok("User updated successfully");
    }

    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication){
        String email = authentication.getName();
        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        if (!bCryptPasswordEncoder.matches(request.getCurrentPassword(), appUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
        }

        appUser.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(appUser);

        return ResponseEntity.ok("Password changed successfully");
    }
}
