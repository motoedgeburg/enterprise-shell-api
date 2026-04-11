package com.enterprise.shellapi.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CompensationRequest {

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0", message = "Base salary must be at least 0")
    private BigDecimal baseSalary;

    @NotBlank(message = "Pay frequency is required")
    private String payFrequency;

    @DecimalMin(value = "0", message = "Bonus target must be at least 0")
    @DecimalMax(value = "100", message = "Bonus target must be at most 100")
    private BigDecimal bonusTarget;

    @Min(value = 0, message = "Stock options must be at least 0")
    private Integer stockOptions;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private Boolean overtimeEligible;
}
