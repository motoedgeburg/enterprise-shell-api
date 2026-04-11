package com.enterprise.shellapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordRequest {

    @Valid
    @NotNull(message = "Personal info is required")
    private PersonalInfoRequest personalInfo;

    @Valid
    @NotNull(message = "Work info is required")
    private WorkInfoRequest workInfo;

    @Valid
    private PreferencesRequest preferences;

    @Valid
    private CompensationRequest compensation;

    @Valid
    private HistoryRequest history;
}
