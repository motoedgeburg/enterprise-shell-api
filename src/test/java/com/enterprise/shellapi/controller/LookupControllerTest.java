package com.enterprise.shellapi.controller;

import com.enterprise.shellapi.dto.LookupsResponse;
import com.enterprise.shellapi.service.LookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LookupController.class)
@WithMockUser
class LookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LookupService lookupService;

    @Test
    void getLookups_returnsAllLookups() throws Exception {
        LookupsResponse response = LookupsResponse.builder()
                .departments(List.of("Engineering", "Marketing"))
                .statuses(List.of("active", "inactive"))
                .employmentTypes(List.of("full-time", "contract"))
                .notificationChannels(List.of("email", "slack"))
                .accessLevels(List.of("standard", "admin"))
                .relationships(List.of("Spouse", "Parent"))
                .build();

        when(lookupService.getAllLookups()).thenReturn(response);

        mockMvc.perform(get("/api/lookups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departments").isArray())
                .andExpect(jsonPath("$.departments[0]").value("Engineering"))
                .andExpect(jsonPath("$.statuses").isArray())
                .andExpect(jsonPath("$.employmentTypes").isArray())
                .andExpect(jsonPath("$.notificationChannels").isArray())
                .andExpect(jsonPath("$.accessLevels").isArray())
                .andExpect(jsonPath("$.relationships").isArray());
    }
}
