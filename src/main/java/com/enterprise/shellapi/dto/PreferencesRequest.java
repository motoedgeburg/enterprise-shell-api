package com.enterprise.shellapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesRequest {

    private Boolean remoteEligible;
    private Boolean notificationsEnabled;
    private List<String> notificationChannels;

    @NotBlank(message = "Access level is required")
    private String accessLevel;

    private String notes;
}
