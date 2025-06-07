package com.david.worktrack.controller;

import com.david.worktrack.dto.UserResponse;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AppUserRepository appUserRepository;

    @GetMapping("/test")
    public ResponseEntity<String> adminTest(){
        return ResponseEntity.ok("Admin test successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        List<AppUser> users = appUserRepository.findAll();

        List<UserResponse> response = users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getAppUserRole()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}
