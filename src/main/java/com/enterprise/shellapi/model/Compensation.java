package com.enterprise.shellapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compensation {

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private Long recordId;

    private BigDecimal baseSalary;
    private String payFrequency;
    private BigDecimal bonusTarget;
    private Integer stockOptions;
    private LocalDate effectiveDate;
    private Boolean overtimeEligible;
}
