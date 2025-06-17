package com.david.worktrack.controller;

import com.david.worktrack.dto.JobApplicationRequest;
import com.david.worktrack.dto.JobApplicationResponse;
import com.david.worktrack.dto.UpdateJobStatusRequest;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.entity.JobApplication;
import com.david.worktrack.repository.AppUserRepository;
import com.david.worktrack.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationRepository jobApplicationRepository;
    private final AppUserRepository appUserRepository;
    private final DefaultAuthenticationEventPublisher authenticationEventPublisher;

    // Create job application
    @PostMapping
    public ResponseEntity<String> createJobApplication(@RequestBody JobApplicationRequest request, Authentication authentication) {

        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() ->  new UsernameNotFoundException("User not found"));

        JobApplication application = JobApplication.builder()
                .companyName(request.getCompanyName())
                .position(request.getPosition())
                .status(request.getStatus())
                .appliedDate(request.getAppliedDate())
                .notes(request.getNotes())
                .appUser(user)
                .build();

        jobApplicationRepository.save(application);

        return ResponseEntity.ok("Job Application created");
    }

    // Get my job application
    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> getUserJobApplications(Authentication authentication) {

        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<JobApplication> applications = jobApplicationRepository.findByAppUser(user);

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
    public ResponseEntity<String> updateJobApplication(@PathVariable Long id, @RequestBody JobApplicationRequest request, Authentication authentication) {

        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        JobApplication app = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Job Application not found"));

        // Security: allow only owner to update
        if(!app.getAppUser().getId().equals(user.getId())) {
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
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        JobApplication app = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Job Application not found"));

        // Only the owner can delete it
        if (!app.getAppUser().getId().equals(user.getId())){
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
    AppUser user = appUserRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    JobApplication app = jobApplicationRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Job Application not found"));

    // Check ownership
    if (!app.getAppUser().getId().equals(user.getId())) {
        return ResponseEntity.status(403).body("You can only update your own applications");
    }

    // ✅ Update status only
    app.setStatus(request.getStatus());
    jobApplicationRepository.save(app);

    return ResponseEntity.ok("Status updated");
}


}
