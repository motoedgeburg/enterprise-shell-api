package com.enterprise.shellapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfo {

    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String ssn;
    private String bio;
}
