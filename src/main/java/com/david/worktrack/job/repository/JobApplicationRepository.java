package com.david.worktrack.job.repository;

import com.david.worktrack.job.entity.JobApplication;
import com.david.worktrack.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository  extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByAppUser(AppUser appUser);

    Optional<JobApplication> findByIdAndAppUser(Long id, AppUser appUser);
}
