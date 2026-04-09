package com.enterprise.shellapi.controller;

import com.enterprise.shellapi.dto.*;
import com.enterprise.shellapi.exception.GlobalExceptionHandler;
import com.enterprise.shellapi.exception.RecordNotFoundException;
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

    private Record buildRecord(Long id, String name, String email) {
        return Record.builder()
                .id(id)
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
                .emergencyContacts(Collections.emptyList())
                .certifications(Collections.emptyList())
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
    void search_returnsPagedResponse() throws Exception {
        Record record = buildRecord(1L, "Alice Johnson", "alice@company.com");

        PagedResponse<Record> response = PagedResponse.<Record>builder()
                .content(List.of(record))
                .totalElements(1)
                .totalPages(1)
                .size(10)
                .number(0)
                .build();

        when(recordService.search(any(), any(), any(), any(), any(), eq(0), eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/api/records")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].personalInfo.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findById_returnsRecord() throws Exception {
        Record record = buildRecord(1L, "Alice Johnson", "alice@company.com");

        when(recordService.findById(1L)).thenReturn(record);

        mockMvc.perform(get("/api/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalInfo.name").value("Alice Johnson"));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        when(recordService.findById(999L)).thenThrow(new RecordNotFoundException(999L));

        mockMvc.perform(get("/api/records/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Record not found with id: 999"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        RecordRequest request = buildRequest("New Person", "new@company.com");
        Record created = buildRecord(9L, "New Person", "new@company.com");

        when(recordService.create(any(RecordRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.personalInfo.name").value("New Person"));
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
        Record updated = buildRecord(1L, "Updated Person", "updated@company.com");

        when(recordService.update(eq(1L), any(RecordRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalInfo.name").value("Updated Person"));
    }

    @Test
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/records/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new RecordNotFoundException(999L)).when(recordService).delete(999L);

        mockMvc.perform(delete("/api/records/999"))
                .andExpect(status().isNotFound());
    }
}
