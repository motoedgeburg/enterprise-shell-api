package com.enterprise.shellapi.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryRequest {

    @Valid
    private List<EmergencyContactRequest> emergencyContacts;

    @Valid
    private List<CertificationRequest> certifications;
}
