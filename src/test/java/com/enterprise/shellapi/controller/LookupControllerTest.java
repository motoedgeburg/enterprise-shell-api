package com.enterprise.shellapi.controller;

import com.enterprise.shellapi.dto.LookupOption;
import com.enterprise.shellapi.dto.LookupsResponse;
import com.enterprise.shellapi.service.LookupService;
import org.junit.jupiter.api.Test;
import com.enterprise.shellapi.config.CorsConfig;
import com.enterprise.shellapi.config.JwtConfig;
import com.enterprise.shellapi.config.SecurityConfig;
import com.enterprise.shellapi.filter.OktaJwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LookupController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, OktaJwtAuthenticationFilter.class, JwtConfig.class, CorsConfig.class}))
class LookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LookupService lookupService;

    @Test
    void getLookups_returnsAllLookups() throws Exception {
        LookupsResponse response = LookupsResponse.builder()
                .departments(List.of(
                        new LookupOption("Engineering", "Engineering"),
                        new LookupOption("Marketing", "Marketing")))
                .statuses(List.of(
                        new LookupOption("active", "Active"),
                        new LookupOption("inactive", "Inactive")))
                .employmentTypes(List.of(
                        new LookupOption("full-time", "Full-time"),
                        new LookupOption("contract", "Contract")))
                .notificationChannels(List.of(
                        new LookupOption("email", "Email"),
                        new LookupOption("slack", "Slack")))
                .accessLevels(List.of(
                        new LookupOption("standard", "Standard"),
                        new LookupOption("admin", "Admin")))
                .relationships(List.of(
                        new LookupOption("Spouse", "Spouse"),
                        new LookupOption("Parent", "Parent")))
                .build();

        when(lookupService.getAllLookups()).thenReturn(response);

        mockMvc.perform(get("/api/lookups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departments").isArray())
                .andExpect(jsonPath("$.departments[0].value").value("Engineering"))
                .andExpect(jsonPath("$.departments[0].label").value("Engineering"))
                .andExpect(jsonPath("$.statuses[0].value").value("active"))
                .andExpect(jsonPath("$.statuses[0].label").value("Active"))
                .andExpect(jsonPath("$.employmentTypes[0].value").value("full-time"))
                .andExpect(jsonPath("$.employmentTypes[0].label").value("Full-time"))
                .andExpect(jsonPath("$.notificationChannels").isArray())
                .andExpect(jsonPath("$.accessLevels").isArray())
                .andExpect(jsonPath("$.relationships[0].value").value("Spouse"))
                .andExpect(jsonPath("$.relationships[0].label").value("Spouse"));
    }
}
