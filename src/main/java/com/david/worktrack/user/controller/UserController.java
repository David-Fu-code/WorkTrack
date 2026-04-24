package com.david.worktrack.user.controller;

import com.david.worktrack.user.entity.AppUser;
import com.david.worktrack.user.dto.ChangePasswordRequest;
import com.david.worktrack.user.dto.UpdateProfileRequest;
import com.david.worktrack.user.dto.UserResponse;
import com.david.worktrack.user.service.UserMapper;
import com.david.worktrack.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {

        AppUser appUser = userService.getUserByEmailOrThrow(authentication.getName());

        return ResponseEntity.ok(UserMapper.toResponse(appUser));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateProfileName(@RequestBody UpdateProfileRequest request, Authentication authentication){

        AppUser appUser = userService.getUserByEmailOrThrow(authentication.getName());

        userService.updateProfileName(request, appUser);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication){

        AppUser appUser = userService.getUserByEmailOrThrow(authentication.getName());

        userService.changePassword(request, appUser);

        return ResponseEntity.noContent().build();
    }
}
