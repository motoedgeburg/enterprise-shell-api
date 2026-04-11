package com.enterprise.shellapi.controller;

import com.enterprise.shellapi.dto.ErrorResponse;
import com.enterprise.shellapi.dto.PersonalInfoRequest;
import com.enterprise.shellapi.dto.PreferencesRequest;
import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.model.History;
import com.enterprise.shellapi.dto.RecordSummary;
import com.enterprise.shellapi.dto.WorkInfoRequest;
import com.enterprise.shellapi.exception.GlobalExceptionHandler;
import com.enterprise.shellapi.exception.RecordNotFoundException;
import com.enterprise.shellapi.exception.ValidationException;
import com.enterprise.shellapi.model.PersonalInfo;
import com.enterprise.shellapi.model.Preferences;
import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.model.WorkInfo;
import com.enterprise.shellapi.service.RecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecordController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, OktaJwtAuthenticationFilter.class, JwtConfig.class, CorsConfig.class}))
@Import(GlobalExceptionHandler.class)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecordService recordService;

    private static final String TEST_UUID = "550e8400-e29b-41d4-a716-446655440000";

    private Record buildRecord(String uuid, String name, String email) {
        return Record.builder()
                .id(1L)
                .uuid(uuid)
                .personalInfo(PersonalInfo.builder()
                        .name(name)
                        .email(email)
                        .build())
                .workInfo(WorkInfo.builder()
                        .status("active")
                        .build())
                .preferences(Preferences.builder()
                        .remoteEligible(false)
                        .notificationsEnabled(true)
                        .notificationChannels(Collections.emptyList())
                        .accessLevel("standard")
                        .build())
                .history(History.builder()
                        .emergencyContacts(Collections.emptyList())
                        .certifications(Collections.emptyList())
                        .build())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private RecordRequest buildRequest(String name, String email) {
        return RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder()
                        .name(name)
                        .email(email)
                        .build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer")
                        .department("Engineering")
                        .status("active")
                        .employmentType("full-time")
                        .build())
                .build();
    }

    @Test
    void search_returnsSummaryList() throws Exception {
        RecordSummary summary = RecordSummary.builder()
                .uuid(TEST_UUID)
                .name("Alice Johnson")
                .address("123 Market St")
                .department("Engineering")
                .status("active")
                .build();

        when(recordService.search(any(), any(), any(), any(), any()))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/api/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].uuid").value(TEST_UUID))
                .andExpect(jsonPath("$[0].name").value("Alice Johnson"))
                .andExpect(jsonPath("$[0].department").value("Engineering"))
                .andExpect(jsonPath("$[0].status").value("active"))
                .andExpect(jsonPath("$[0].personalInfo").doesNotExist());
    }

    @Test
    void findByUuid_returnsRecord() throws Exception {
        Record record = buildRecord(TEST_UUID, "Alice Johnson", "alice@company.com");

        when(recordService.findByUuid(TEST_UUID)).thenReturn(record);

        mockMvc.perform(get("/api/records/" + TEST_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(TEST_UUID))
                .andExpect(jsonPath("$.personalInfo.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void findByUuid_notFound_returns404() throws Exception {
        String badUuid = "00000000-0000-0000-0000-000000000000";
        when(recordService.findByUuid(badUuid)).thenThrow(new RecordNotFoundException(badUuid));

        mockMvc.perform(get("/api/records/" + badUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Record not found: " + badUuid))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        RecordRequest request = buildRequest("New Person", "new@company.com");
        Record created = buildRecord(TEST_UUID, "New Person", "new@company.com");

        when(recordService.create(any(RecordRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(TEST_UUID))
                .andExpect(jsonPath("$.personalInfo.name").value("New Person"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void create_missingPersonalInfo_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer")
                        .department("Engineering")
                        .status("active")
                        .employmentType("full-time")
                        .build())
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingName_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder()
                        .email("new@company.com")
                        .build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer")
                        .department("Engineering")
                        .status("active")
                        .employmentType("full-time")
                        .build())
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void create_invalidEmail_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder()
                        .name("Test")
                        .email("not-an-email")
                        .build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer")
                        .department("Engineering")
                        .status("active")
                        .employmentType("full-time")
                        .build())
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_validRequest_returns200() throws Exception {
        RecordRequest request = buildRequest("Updated Person", "updated@company.com");
        Record updated = buildRecord(TEST_UUID, "Updated Person", "updated@company.com");

        when(recordService.update(eq(TEST_UUID), any(RecordRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/records/" + TEST_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalInfo.name").value("Updated Person"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/records/" + TEST_UUID))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        String badUuid = "00000000-0000-0000-0000-000000000000";
        doThrow(new RecordNotFoundException(badUuid)).when(recordService).delete(badUuid);

        mockMvc.perform(delete("/api/records/" + badUuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_invalidPhone_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder()
                        .name("Test").email("test@company.com").phone("12345").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("personalInfo.phone"));
    }

    @Test
    void create_invalidSsn_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder()
                        .name("Test").email("test@company.com").ssn("12345").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("personalInfo.ssn"));
    }

    @Test
    void create_invalidLookupValue_returns400() throws Exception {
        RecordRequest request = buildRequest("Test", "test@company.com");

        when(recordService.create(any(RecordRequest.class)))
                .thenThrow(new ValidationException(List.of(
                        ErrorResponse.FieldError.builder()
                                .field("workInfo.department")
                                .message("Must be one of: Engineering, Product, Design")
                                .build())));

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("workInfo.department"));
    }

    @Test
    void create_bioTooLong_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder()
                        .name("Test").email("test@company.com")
                        .bio("x".repeat(501)).build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("personalInfo.bio"));
    }

    @Test
    void create_addressTooShort_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder()
                        .name("Test").email("test@company.com")
                        .address("abc").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("personalInfo.address"));
    }

    @Test
    void create_notesTooLong_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder()
                        .name("Test").email("test@company.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .preferences(PreferencesRequest.builder()
                        .accessLevel("standard")
                        .notes("x".repeat(1001)).build())
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("preferences.notes"));
    }
}
