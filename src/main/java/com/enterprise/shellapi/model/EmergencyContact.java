package com.enterprise.shellapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {

    private Long id;
    private Long recordId;
    private String name;
    private String relationship;
    private String phone;
    private String email;
    private Boolean isPrimary;
}
