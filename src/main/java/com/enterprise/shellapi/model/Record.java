package com.enterprise.shellapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Record {

    @JsonIgnore
    private Long id;

    private String uuid;
    private PersonalInfo personalInfo;
    private WorkInfo workInfo;
    private Preferences preferences;
    private Compensation compensation;
    private History history;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
