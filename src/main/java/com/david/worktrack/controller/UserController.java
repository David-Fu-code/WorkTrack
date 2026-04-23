package com.david.worktrack.controller;

import com.david.worktrack.dto.*;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.service.AuthService;
import com.david.worktrack.service.token.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUser(Authentication authentication) {

        AppUser appUser = authService.getCurrentUser(authentication);

        UserResponse response = userService.getUser(appUser);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateProfileName(@RequestBody UpdateProfileRequest request, Authentication authentication){

        AppUser appUser = authService.getCurrentUser(authentication);

        userService.updateProfileName(request, appUser);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication){

        AppUser appUser = authService.getCurrentUser(authentication);

        userService.changePassword(request, appUser);

        return ResponseEntity.noContent().build();
    }
}
