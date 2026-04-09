package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.PagedResponse;
import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.exception.RecordNotFoundException;
import com.enterprise.shellapi.model.Certification;
import com.enterprise.shellapi.model.EmergencyContact;
import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.repository.CertificationRepository;
import com.enterprise.shellapi.repository.EmergencyContactRepository;
import com.enterprise.shellapi.repository.RecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private RecordService recordService;

    @Test
    void search_returnsPagedResponse() {
        Record record = Record.builder().id(1L).name("Alice").email("alice@test.com").build();
        when(recordRepository.search(any(), any(), any(), any(), any(), eq(10), eq(0)))
                .thenReturn(List.of(record));
        when(recordRepository.count(any(), any(), any(), any(), any())).thenReturn(1L);
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        PagedResponse<Record> result = recordService.search(null, null, null, null, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();
    }

    @Test
    void findById_existingRecord_returnsRecord() {
        Record record = Record.builder().id(1L).name("Alice").email("alice@test.com").build();
        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(
                List.of(EmergencyContact.builder().id(1L).name("Contact").build()));
        when(certificationRepository.findByRecordId(1L)).thenReturn(
                List.of(Certification.builder().id(1L).name("AWS").build()));

        Record result = recordService.findById(1L);

        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getEmergencyContacts()).hasSize(1);
        assertThat(result.getCertifications()).hasSize(1);
    }

    @Test
    void findById_notFound_throwsException() {
        when(recordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.findById(999L))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void create_savesRecordAndRelations() {
        RecordRequest request = RecordRequest.builder()
                .name("New Person")
                .email("new@test.com")
                .build();

        when(recordRepository.insert(any(Record.class))).thenReturn(5L);
        Record saved = Record.builder().id(5L).name("New Person").email("new@test.com").build();
        when(recordRepository.findById(5L)).thenReturn(Optional.of(saved));
        when(emergencyContactRepository.findByRecordId(5L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(5L)).thenReturn(Collections.emptyList());

        Record result = recordService.create(request);

        assertThat(result.getId()).isEqualTo(5L);
        verify(recordRepository).insert(any(Record.class));
    }

    @Test
    void update_existingRecord_updatesAndReturns() {
        Record existing = Record.builder().id(1L).name("Old").email("old@test.com").build();
        when(recordRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(recordRepository.update(eq(1L), any(Record.class))).thenReturn(1);

        Record updated = Record.builder().id(1L).name("Updated").email("updated@test.com").build();
        // Second call to findById is after update
        when(recordRepository.findById(1L))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(updated));
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        RecordRequest request = RecordRequest.builder()
                .name("Updated")
                .email("updated@test.com")
                .build();

        Record result = recordService.update(1L, request);

        verify(emergencyContactRepository).deleteByRecordId(1L);
        verify(certificationRepository).deleteByRecordId(1L);
    }

    @Test
    void update_notFound_throwsException() {
        when(recordRepository.findById(999L)).thenReturn(Optional.empty());

        RecordRequest request = RecordRequest.builder()
                .name("Test")
                .email("test@test.com")
                .build();

        assertThatThrownBy(() -> recordService.update(999L, request))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void delete_existingRecord_deletes() {
        Record existing = Record.builder().id(1L).name("Alice").build();
        when(recordRepository.findById(1L)).thenReturn(Optional.of(existing));

        recordService.delete(1L);

        verify(recordRepository).delete(1L);
    }

    @Test
    void delete_notFound_throwsException() {
        when(recordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.delete(999L))
                .isInstanceOf(RecordNotFoundException.class);
    }
}
