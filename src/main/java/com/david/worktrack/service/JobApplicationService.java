package com.david.worktrack.service;

import com.david.worktrack.dto.JobApplicationRequest;
import com.david.worktrack.dto.JobApplicationResponse;
import com.david.worktrack.dto.UpdateJobStatusRequest;
import com.david.worktrack.entity.AppUser;
import com.david.worktrack.entity.JobApplication;
import com.david.worktrack.exception.ResourceNotFoundException;
import com.david.worktrack.repository.JobApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationService {

    private final JobApplicationRepository repository;

    public void createJobApplication(JobApplicationRequest request, AppUser appUser) {

        JobApplication application = JobApplication.builder()
                .companyName(request.getCompanyName())
                .position(request.getPosition())
                .status(request.getStatus())
                .appliedDate(request.getAppliedDate())
                .notes(request.getNotes())
                .appUser(appUser)
                .build();

        repository.save(application);
    }

    public List<JobApplicationResponse> getUserJobApplications(AppUser appUser) {

        return repository.findByAppUser(appUser)
                .stream()
                .map(app -> new JobApplicationResponse(
                        app.getId(),
                        app.getCompanyName(),
                        app.getPosition(),
                        app.getStatus(),
                        app.getAppliedDate(),
                        app.getNotes()
                ))
                .toList();
    }

    public void update(Long id, JobApplicationRequest request, AppUser appUser) {

        JobApplication app = getUserApplicationOrThrow(id, appUser);

        app.setCompanyName(request.getCompanyName());
        app.setPosition(request.getPosition());
        app.setStatus(request.getStatus());
        app.setAppliedDate(request.getAppliedDate());
        app.setNotes(request.getNotes());
    }

    public void delete(Long id, AppUser appUser) {

        JobApplication app = getUserApplicationOrThrow(id, appUser);

        repository.delete(app);
    }

    public void updateJobStatus(Long id, UpdateJobStatusRequest request, AppUser appUser) {

        JobApplication app = getUserApplicationOrThrow(id, appUser);

        // Update status | No save, use @Transactional
        app.setStatus(request.getStatus());
    }

    public JobApplication getUserApplicationOrThrow(Long id, AppUser appUser) {

        return repository.findByIdAndAppUser(id, appUser)
                .orElseThrow(() -> new ResourceNotFoundException("Job Application not found"));
    }

}
