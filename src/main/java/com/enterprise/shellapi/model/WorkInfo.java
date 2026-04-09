package com.enterprise.shellapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkInfo {

    private String jobTitle;
    private String manager;
    private String department;
    private String status;
    private LocalDate startDate;
    private String employmentType;
}
