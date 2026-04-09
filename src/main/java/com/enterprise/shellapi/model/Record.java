package com.enterprise.shellapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Record {

    private Long id;
    private String name;
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
    private LocalDateTime createdAt;
    private List<EmergencyContact> emergencyContacts;
    private List<Certification> certifications;
}
