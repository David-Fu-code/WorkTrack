package com.david.worktrack.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class JobApplicationRequest {
    private String companyName;
    private String position;
    private String status;
    private LocalDate appliedDate;
    private String notes;
}
