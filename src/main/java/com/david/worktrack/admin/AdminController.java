package com.david.worktrack.admin;

import com.david.worktrack.user.dto.UserResponse;
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

    private final AdminService adminService;

    @GetMapping("/test")
    public ResponseEntity<String> adminTest(){
        return ResponseEntity.ok("Admin test successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(){

        return ResponseEntity.ok(adminService.getAllUsers());
    }
}
