package com.david.worktrack.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class JobApplicationResponse {
    private Long id;
    private String companyName;
    private String position;
    private String status;
    private LocalDate appliedDate;
    private String notes;
}
