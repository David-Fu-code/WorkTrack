package com.david.worktrack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "job_application")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String position;
    private String status; // APPLIED, INTERVIEW, OFFER, REJECTED, etc.

    private LocalDate appliedDate;

    @Column(length = 2000)
    private String notes;

    // Link to the User who owns this application
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser appUser;
}
