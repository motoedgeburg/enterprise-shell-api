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
public class Preferences {

    private Boolean remoteEligible;
    private Boolean notificationsEnabled;
    private List<String> notificationChannels;
    private String accessLevel;
    private String notes;
}
