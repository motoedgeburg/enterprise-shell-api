package com.enterprise.shellapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkInfoRequest {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String manager;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Status is required")
    private String status;

    private LocalDate startDate;

    @NotBlank(message = "Employment type is required")
    private String employmentType;
}
