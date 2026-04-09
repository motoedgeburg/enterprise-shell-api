package com.enterprise.shellapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordSummary {

    private String uuid;
    private String name;
    private String address;
    private String department;
    private String status;
}
