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

    private List<LookupOption> departments;
    private List<LookupOption> relationships;
    private List<LookupOption> statuses;
    private List<LookupOption> employmentTypes;
    private List<LookupOption> notificationChannels;
    private List<LookupOption> accessLevels;
    private List<LookupOption> payFrequencies;
}
