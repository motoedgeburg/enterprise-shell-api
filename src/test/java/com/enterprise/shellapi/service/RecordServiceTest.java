package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.PagedResponse;
import com.enterprise.shellapi.dto.PersonalInfoRequest;
import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.dto.WorkInfoRequest;
import com.enterprise.shellapi.exception.RecordNotFoundException;
import com.enterprise.shellapi.model.Certification;
import com.enterprise.shellapi.model.EmergencyContact;
import com.enterprise.shellapi.model.PersonalInfo;
import com.enterprise.shellapi.model.Preferences;
import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.model.WorkInfo;
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

    private Record buildRecord(Long id, String name, String email) {
        return Record.builder()
                .id(id)
                .personalInfo(PersonalInfo.builder().name(name).email(email).build())
                .workInfo(WorkInfo.builder().status("active").build())
                .preferences(Preferences.builder()
                        .remoteEligible(false)
                        .notificationsEnabled(true)
                        .notificationChannels(Collections.emptyList())
                        .accessLevel("standard")
                        .build())
                .build();
    }

    private RecordRequest buildRequest(String name, String email) {
        return RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name(name).email(email).build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer")
                        .department("Engineering")
                        .status("active")
                        .employmentType("full-time")
                        .build())
                .build();
    }

    @Test
    void search_returnsPagedResponse() {
        Record record = buildRecord(1L, "Alice", "alice@test.com");
        when(recordRepository.search(any(), any(), any(), any(), any(), eq(10), eq(0)))
                .thenReturn(List.of(record));
        when(recordRepository.count(any(), any(), any(), any(), any())).thenReturn(1L);
        when(emergencyContactRepository.findByRecordIds(List.of(1L))).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordIds(List.of(1L))).thenReturn(Collections.emptyList());

        PagedResponse<Record> result = recordService.search(null, null, null, null, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();
    }

    @Test
    void findById_existingRecord_returnsRecord() {
        Record record = buildRecord(1L, "Alice", "alice@test.com");
        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(
                List.of(EmergencyContact.builder().id(1L).name("Contact").build()));
        when(certificationRepository.findByRecordId(1L)).thenReturn(
                List.of(Certification.builder().id(1L).name("AWS").build()));

        Record result = recordService.findById(1L);

        assertThat(result.getPersonalInfo().getName()).isEqualTo("Alice");
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
        RecordRequest request = buildRequest("New Person", "new@test.com");

        when(recordRepository.insert(any(Record.class))).thenReturn(5L);
        Record saved = buildRecord(5L, "New Person", "new@test.com");
        when(recordRepository.findById(5L)).thenReturn(Optional.of(saved));
        when(emergencyContactRepository.findByRecordId(5L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(5L)).thenReturn(Collections.emptyList());

        Record result = recordService.create(request);

        assertThat(result.getId()).isEqualTo(5L);
        verify(recordRepository).insert(any(Record.class));
    }

    @Test
    void update_existingRecord_updatesAndReturns() {
        Record existing = buildRecord(1L, "Old", "old@test.com");
        Record updated = buildRecord(1L, "Updated", "updated@test.com");

        when(recordRepository.findById(1L))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(updated));
        when(recordRepository.update(eq(1L), any(Record.class))).thenReturn(1);
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        RecordRequest request = buildRequest("Updated", "updated@test.com");

        Record result = recordService.update(1L, request);

        verify(recordRepository).update(eq(1L), any(Record.class));
    }

    @Test
    void update_notFound_throwsException() {
        when(recordRepository.findById(999L)).thenReturn(Optional.empty());

        RecordRequest request = buildRequest("Test", "test@test.com");

        assertThatThrownBy(() -> recordService.update(999L, request))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void delete_existingRecord_deletes() {
        Record existing = buildRecord(1L, "Alice", "alice@test.com");
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
