package com.david.worktrack.job.controller;

import com.david.worktrack.job.service.JobApplicationService;
import com.david.worktrack.job.dto.JobApplicationRequest;
import com.david.worktrack.job.dto.JobApplicationResponse;
import com.david.worktrack.job.dto.UpdateJobStatusRequest;
import com.david.worktrack.user.entity.AppUser;
import com.david.worktrack.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final AuthService authService;

    // Create job application
    @PostMapping
    public ResponseEntity<Void> createJobApplication(@RequestBody JobApplicationRequest request, Authentication authentication) {

        AppUser appUser = authService.getCurrentUser(authentication);

        jobApplicationService.createJobApplication(request, appUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Get all my  Job applications
    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> getUserJobApplications(Authentication authentication) {

        AppUser appUser = authService.getCurrentUser(authentication);

        List<JobApplicationResponse> response = jobApplicationService.getUserJobApplications(appUser);

        return ResponseEntity.ok(response);
    }

    // Update Job Application
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateJobApplication(@PathVariable("id") Long id, @RequestBody JobApplicationRequest request, Authentication authentication) {

        AppUser appUser = authService.getCurrentUser(authentication);

        jobApplicationService.updateJobApplication(id, request, appUser);

        return ResponseEntity.ok().build();

    }

    // Delete Job Application
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobApplication(@PathVariable("id") Long id, Authentication authentication) {

        AppUser appUser = authService.getCurrentUser(authentication);

        jobApplicationService.deleteJobApplication(id, appUser);

        return ResponseEntity.noContent().build();
    }

    // Update Job Status
    @PatchMapping("/{id}") // Change Status
    public ResponseEntity<Void> updateJobStatus(@PathVariable Long id, @RequestBody UpdateJobStatusRequest request, Authentication authentication) {

        AppUser appUser = authService.getCurrentUser(authentication);

        jobApplicationService.updateJobStatus(id, request, appUser);

        return ResponseEntity.ok().build();
    }


}
