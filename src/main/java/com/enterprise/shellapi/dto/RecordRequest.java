package com.enterprise.shellapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String ssn;
    private String bio;
    private String department;
    private String jobTitle;
    private String employmentType;
    private LocalDate startDate;
    private String manager;
    private String status;
    private Boolean remoteEligible;
    private Boolean notificationsEnabled;
    private List<String> notificationChannels;
    private String accessLevel;
    private String notes;

    @Valid
    private List<EmergencyContactRequest> emergencyContacts;

    @Valid
    private List<CertificationRequest> certifications;
}
