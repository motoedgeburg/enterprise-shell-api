package com.enterprise.shellapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LookupsResponse {

    private List<String> departments;
    private List<String> relationships;
    private List<String> statuses;
    private List<String> employmentTypes;
    private List<String> notificationChannels;
    private List<String> accessLevels;
}
