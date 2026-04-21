package com.david.worktrack.controller;

import com.david.worktrack.dto.JobApplicationRequest;
import com.david.worktrack.dto.JobApplicationResponse;
import com.david.worktrack.dto.UpdateJobStatusRequest;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.entity.JobApplication;
import com.david.worktrack.exception.ResourceNotFoundException;
import com.david.worktrack.repository.JobApplicationRepository;
import com.david.worktrack.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationRepository jobApplicationRepository;
    private final AppUserService appUserService;

    // Create job application
    @PostMapping
    public ResponseEntity<String> createJobApplication(@RequestBody JobApplicationRequest request, Authentication authentication) {

        String email = authentication.getName();
        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        JobApplication application = JobApplication.builder()
                .companyName(request.getCompanyName())
                .position(request.getPosition())
                .status(request.getStatus())
                .appliedDate(request.getAppliedDate())
                .notes(request.getNotes())
                .appUser(appUser)
                .build();

        jobApplicationRepository.save(application);

        return ResponseEntity.ok("Job Application created");
    }

    // Get my job application
    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> getUserJobApplications(Authentication authentication) {

        String email = authentication.getName();
        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        List<JobApplication> applications = jobApplicationRepository.findByAppUser(appUser);

        List<JobApplicationResponse> response = applications.stream()
                .map(app -> new JobApplicationResponse(
                        app.getId(),
                        app.getCompanyName(),
                        app.getPosition(),
                        app.getStatus(),
                        app.getAppliedDate(),
                        app.getNotes()
                )).toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateJobApplication(@PathVariable("id") Long id, @RequestBody JobApplicationRequest request, Authentication authentication) {

        String email = authentication.getName();
        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        JobApplication app = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job Application not found"));

        // Security: allow only owner to update
        if(!app.getAppUser().getId().equals(appUser.getId())) {
            return ResponseEntity.status(403).body("You can only update your own applications");
        }

        // Update fields
        app.setCompanyName(request.getCompanyName());
        app.setPosition(request.getPosition());
        app.setStatus(request.getStatus());
        app.setAppliedDate(request.getAppliedDate());
        app.setNotes(request.getNotes());

        jobApplicationRepository.save(app);

        return ResponseEntity.ok("Job Application updated");

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJobApplication(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        JobApplication app = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job Application not found"));

        // Only the owner can delete it
        if (!app.getAppUser().getId().equals(appUser.getId())){
            return ResponseEntity.status(403).body("You can only delete your own applications");
        }

        jobApplicationRepository.delete(app);

        return ResponseEntity.ok("Job Application deleted");
    }

    @PatchMapping("/{id}") // Change Status
    public ResponseEntity<String> updateJobStatus(
            @PathVariable Long id,
            @RequestBody UpdateJobStatusRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        AppUser appUser = appUserService.getUserByEmailOrThrow(email);

        JobApplication app = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job Application not found"));

        // Check ownership
        if (!app.getAppUser().getId().equals(appUser.getId())) {
            return ResponseEntity.status(403).body("You can only update your own applications");
        }

        // ✅ Update status only
        app.setStatus(request.getStatus());
        jobApplicationRepository.save(app);

        return ResponseEntity.ok("Status updated");
    }


}
