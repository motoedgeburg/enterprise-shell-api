package com.enterprise.shellapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class History {

    private List<EmergencyContact> emergencyContacts;
    private List<Certification> certifications;
}
