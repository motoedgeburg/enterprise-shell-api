package com.enterprise.shellapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfoRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "\\(\\d{3}\\) \\d{3}-\\d{4}", message = "Phone must match format (NNN) NNN-NNNN")
    private String phone;

    @Size(min = 5, message = "Address must be at least 5 characters")
    private String address;

    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "\\d{3}-\\d{2}-\\d{4}", message = "SSN must match format NNN-NN-NNNN")
    private String ssn;

    @Size(max = 500, message = "Bio must be 500 characters or less")
    private String bio;
}
