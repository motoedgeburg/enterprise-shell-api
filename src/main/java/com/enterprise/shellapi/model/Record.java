package com.enterprise.shellapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Record {

    private Long id;
    private PersonalInfo personalInfo;
    private WorkInfo workInfo;
    private Preferences preferences;
    private List<EmergencyContact> emergencyContacts;
    private List<Certification> certifications;
    private LocalDateTime createdAt;
}
