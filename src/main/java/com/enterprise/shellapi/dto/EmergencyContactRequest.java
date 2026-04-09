package com.enterprise.shellapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactRequest {

    @NotBlank(message = "Emergency contact name is required")
    private String name;

    private String relationship;
    private String phone;
    private String email;
    private Boolean isPrimary;
}
