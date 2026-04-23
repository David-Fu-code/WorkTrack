package com.david.worktrack.repository;

import com.david.worktrack.entity.AppUser;
import com.david.worktrack.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository  extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByAppUser(AppUser appUser);

    Optional<JobApplication> findByIdAndAppUser(Long id, AppUser appUser);
}
