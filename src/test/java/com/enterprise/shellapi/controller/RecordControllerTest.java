package com.enterprise.shellapi.controller;

import com.enterprise.shellapi.dto.PagedResponse;
import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.exception.GlobalExceptionHandler;
import com.enterprise.shellapi.exception.RecordNotFoundException;
import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.service.RecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

@WebMvcTest(RecordController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecordService recordService;

    @Test
    void search_returnsPagedResponse() throws Exception {
        Record record = Record.builder()
                .id(1L)
                .name("Alice Johnson")
                .email("alice@company.com")
                .status("active")
                .createdAt(LocalDateTime.now())
                .emergencyContacts(Collections.emptyList())
                .certifications(Collections.emptyList())
                .build();

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
                .andExpect(jsonPath("$.content[0].name").value("Alice Johnson"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findById_returnsRecord() throws Exception {
        Record record = Record.builder()
                .id(1L)
                .name("Alice Johnson")
                .email("alice@company.com")
                .emergencyContacts(Collections.emptyList())
                .certifications(Collections.emptyList())
                .build();

        when(recordService.findById(1L)).thenReturn(record);

        mockMvc.perform(get("/api/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Johnson"));
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
        RecordRequest request = RecordRequest.builder()
                .name("New Person")
                .email("new@company.com")
                .build();

        Record created = Record.builder()
                .id(9L)
                .name("New Person")
                .email("new@company.com")
                .emergencyContacts(Collections.emptyList())
                .certifications(Collections.emptyList())
                .build();

        when(recordService.create(any(RecordRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.name").value("New Person"));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .email("new@company.com")
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void create_invalidEmail_returns400() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .name("Test")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void update_validRequest_returns200() throws Exception {
        RecordRequest request = RecordRequest.builder()
                .name("Updated Person")
                .email("updated@company.com")
                .build();

        Record updated = Record.builder()
                .id(1L)
                .name("Updated Person")
                .email("updated@company.com")
                .emergencyContacts(Collections.emptyList())
                .certifications(Collections.emptyList())
                .build();

        when(recordService.update(eq(1L), any(RecordRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Person"));
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
