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
public class Certification {

    private Long id;
    private Long recordId;
    private String name;
    private String issuingBody;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String credentialId;
}
