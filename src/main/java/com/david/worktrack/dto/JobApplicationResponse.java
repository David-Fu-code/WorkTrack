package com.david.worktrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
