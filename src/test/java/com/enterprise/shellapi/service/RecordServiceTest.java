package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.CertificationRequest;
import com.enterprise.shellapi.dto.CompensationRequest;
import com.enterprise.shellapi.dto.EmergencyContactRequest;
import com.enterprise.shellapi.dto.HistoryRequest;
import com.enterprise.shellapi.dto.PersonalInfoRequest;
import com.enterprise.shellapi.dto.PreferencesRequest;
import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.dto.RecordSummary;
import com.enterprise.shellapi.dto.WorkInfoRequest;
import com.enterprise.shellapi.exception.RecordNotFoundException;
import com.enterprise.shellapi.model.Certification;
import com.enterprise.shellapi.model.Compensation;
import com.enterprise.shellapi.model.EmergencyContact;
import com.enterprise.shellapi.model.PersonalInfo;
import com.enterprise.shellapi.model.Preferences;
import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.model.WorkInfo;
import com.enterprise.shellapi.repository.CertificationRepository;
import com.enterprise.shellapi.repository.CompensationRepository;
import com.enterprise.shellapi.repository.EmergencyContactRepository;
import com.enterprise.shellapi.repository.RecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    @Mock
    private CompensationRepository compensationRepository;

    @Mock
    private RecordRequestValidator validator;

    @InjectMocks
    private RecordService recordService;

    private static final String TEST_UUID = "550e8400-e29b-41d4-a716-446655440000";

    private Record buildRecord(Long id, String uuid, String name, String email) {
        return Record.builder()
                .id(id)
                .uuid(uuid)
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
    void search_returnsSummaryList() {
        RecordSummary summary = RecordSummary.builder()
                .uuid(TEST_UUID).name("Alice").department("Engineering").status("active").build();
        when(recordRepository.search(any(), any(), any(), any(), any()))
                .thenReturn(List.of(summary));

        List<RecordSummary> result = recordService.search(null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(TEST_UUID);
        assertThat(result.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void findByUuid_existingRecord_returnsRecord() {
        Record record = buildRecord(1L, TEST_UUID, "Alice", "alice@test.com");
        when(recordRepository.findByUuid(TEST_UUID)).thenReturn(Optional.of(record));
        when(compensationRepository.findByRecordId(1L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(
                List.of(EmergencyContact.builder().id(1L).name("Contact").build()));
        when(certificationRepository.findByRecordId(1L)).thenReturn(
                List.of(Certification.builder().id(1L).name("AWS").build()));

        Record result = recordService.findByUuid(TEST_UUID);

        assertThat(result.getUuid()).isEqualTo(TEST_UUID);
        assertThat(result.getPersonalInfo().getName()).isEqualTo("Alice");
        assertThat(result.getHistory().getEmergencyContacts()).hasSize(1);
        assertThat(result.getHistory().getCertifications()).hasSize(1);
    }

    @Test
    void findByUuid_notFound_throwsException() {
        when(recordRepository.findByUuid("bad-uuid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.findByUuid("bad-uuid"))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessageContaining("bad-uuid");
    }

    @Test
    void create_savesRecordAndRelations() {
        RecordRequest request = buildRequest("New Person", "new@test.com");

        when(recordRepository.insert(any(Record.class))).thenReturn(5L);
        Record saved = buildRecord(5L, TEST_UUID, "New Person", "new@test.com");
        when(recordRepository.findById(5L)).thenReturn(Optional.of(saved));
        when(compensationRepository.findByRecordId(5L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(5L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(5L)).thenReturn(Collections.emptyList());

        Record result = recordService.create(request);

        assertThat(result.getUuid()).isEqualTo(TEST_UUID);
        verify(recordRepository).insert(any(Record.class));
    }

    @Test
    void update_existingRecord_updatesAndReturns() {
        Record existing = buildRecord(1L, TEST_UUID, "Old", "old@test.com");
        Record updated = buildRecord(1L, TEST_UUID, "Updated", "updated@test.com");

        when(recordRepository.findByUuid(TEST_UUID))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(updated));
        when(recordRepository.update(eq(TEST_UUID), any(Record.class))).thenReturn(1);
        when(compensationRepository.findByRecordId(1L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        RecordRequest request = buildRequest("Updated", "updated@test.com");

        Record result = recordService.update(TEST_UUID, request);

        verify(recordRepository).update(eq(TEST_UUID), any(Record.class));
    }

    @Test
    void update_notFound_throwsException() {
        when(recordRepository.findByUuid("bad-uuid")).thenReturn(Optional.empty());

        RecordRequest request = buildRequest("Test", "test@test.com");

        assertThatThrownBy(() -> recordService.update("bad-uuid", request))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void delete_existingRecord_deletes() {
        Record existing = buildRecord(1L, TEST_UUID, "Alice", "alice@test.com");
        when(recordRepository.findByUuid(TEST_UUID)).thenReturn(Optional.of(existing));

        recordService.delete(TEST_UUID);

        verify(recordRepository).delete(TEST_UUID);
    }

    @Test
    void delete_notFound_throwsException() {
        when(recordRepository.findByUuid("bad-uuid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.delete("bad-uuid"))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void create_withHistory_savesContactsAndCerts() {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("Test").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .history(HistoryRequest.builder()
                        .emergencyContacts(List.of(EmergencyContactRequest.builder()
                                .name("Contact").relationship("Spouse").phone("(555) 111-2222").build()))
                        .certifications(List.of(CertificationRequest.builder()
                                .name("AWS").issuingBody("Amazon").issueDate(LocalDate.of(2024, 1, 1)).build()))
                        .build())
                .build();

        when(recordRepository.insert(any(Record.class))).thenReturn(10L);
        Record saved = buildRecord(10L, TEST_UUID, "Test", "t@t.com");
        when(recordRepository.findById(10L)).thenReturn(Optional.of(saved));
        when(compensationRepository.findByRecordId(10L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());

        recordService.create(request);

        verify(emergencyContactRepository).insert(any(EmergencyContact.class));
        verify(certificationRepository).insert(any(Certification.class));
    }

    @Test
    void create_withNullHistory_skipsRelations() {
        RecordRequest request = buildRequest("Test", "t@t.com");

        when(recordRepository.insert(any(Record.class))).thenReturn(10L);
        Record saved = buildRecord(10L, TEST_UUID, "Test", "t@t.com");
        when(recordRepository.findById(10L)).thenReturn(Optional.of(saved));
        when(compensationRepository.findByRecordId(10L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());

        recordService.create(request);

        verify(emergencyContactRepository, never()).insert(any(EmergencyContact.class));
        verify(certificationRepository, never()).insert(any(Certification.class));
    }

    @Test
    void create_withPreferences_mapsAllFields() {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("Test").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .preferences(PreferencesRequest.builder()
                        .remoteEligible(true)
                        .notificationsEnabled(false)
                        .notificationChannels(List.of("email", "slack"))
                        .accessLevel("elevated")
                        .notes("Some notes")
                        .build())
                .build();

        when(recordRepository.insert(any(Record.class))).thenReturn(10L);
        Record saved = buildRecord(10L, TEST_UUID, "Test", "t@t.com");
        when(recordRepository.findById(10L)).thenReturn(Optional.of(saved));
        when(compensationRepository.findByRecordId(10L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());

        recordService.create(request);

        verify(recordRepository).insert(argThat(record ->
                record.getPreferences().getRemoteEligible() &&
                !record.getPreferences().getNotificationsEnabled() &&
                record.getPreferences().getAccessLevel().equals("elevated")));
    }

    @Test
    void create_withNullPreferences_usesDefaults() {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("Test").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .build();

        when(recordRepository.insert(any(Record.class))).thenReturn(10L);
        Record saved = buildRecord(10L, TEST_UUID, "Test", "t@t.com");
        when(recordRepository.findById(10L)).thenReturn(Optional.of(saved));
        when(compensationRepository.findByRecordId(10L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());

        recordService.create(request);

        verify(recordRepository).insert(argThat(record ->
                !record.getPreferences().getRemoteEligible() &&
                record.getPreferences().getNotificationsEnabled() &&
                record.getPreferences().getAccessLevel().equals("standard")));
    }

    @Test
    void update_withHistory_syncsContactsAndCerts() {
        Record existing = buildRecord(1L, TEST_UUID, "Old", "old@test.com");
        Record updated = buildRecord(1L, TEST_UUID, "Updated", "updated@test.com");

        when(recordRepository.findByUuid(TEST_UUID))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(updated));
        when(recordRepository.update(eq(TEST_UUID), any(Record.class))).thenReturn(1);
        when(compensationRepository.findByRecordId(1L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("Updated").email("updated@test.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .history(HistoryRequest.builder()
                        .emergencyContacts(List.of(EmergencyContactRequest.builder()
                                .name("New Contact").relationship("Parent").build()))
                        .certifications(List.of(CertificationRequest.builder()
                                .name("New Cert").issuingBody("Org").issueDate(LocalDate.of(2024, 1, 1)).build()))
                        .build())
                .build();

        recordService.update(TEST_UUID, request);

        verify(emergencyContactRepository).insert(any(EmergencyContact.class));
        verify(certificationRepository).insert(any(Certification.class));
        verify(emergencyContactRepository).deleteByRecordIdExcluding(eq(1L), anyList());
        verify(certificationRepository).deleteByRecordIdExcluding(eq(1L), anyList());
    }

    @Test
    void update_withExistingContactIds_updatesInstead() {
        Record existing = buildRecord(1L, TEST_UUID, "Old", "old@test.com");
        Record updated = buildRecord(1L, TEST_UUID, "Updated", "updated@test.com");

        when(recordRepository.findByUuid(TEST_UUID))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(updated));
        when(recordRepository.update(eq(TEST_UUID), any(Record.class))).thenReturn(1);
        when(compensationRepository.findByRecordId(1L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("Updated").email("updated@test.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .history(HistoryRequest.builder()
                        .emergencyContacts(List.of(EmergencyContactRequest.builder()
                                .id(5L).name("Existing Contact").relationship("Spouse").build()))
                        .certifications(List.of(CertificationRequest.builder()
                                .id(3L).name("Existing Cert").issuingBody("Org").issueDate(LocalDate.of(2024, 1, 1)).build()))
                        .build())
                .build();

        recordService.update(TEST_UUID, request);

        verify(emergencyContactRepository).update(any(EmergencyContact.class));
        verify(certificationRepository).update(any(Certification.class));
        verify(emergencyContactRepository, never()).insert(any(EmergencyContact.class));
        verify(certificationRepository, never()).insert(any(Certification.class));
    }

    @Test
    void update_withNullHistory_deletesAllRelations() {
        Record existing = buildRecord(1L, TEST_UUID, "Old", "old@test.com");
        Record updated = buildRecord(1L, TEST_UUID, "Updated", "updated@test.com");

        when(recordRepository.findByUuid(TEST_UUID))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(updated));
        when(recordRepository.update(eq(TEST_UUID), any(Record.class))).thenReturn(1);
        when(compensationRepository.findByRecordId(1L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        RecordRequest request = buildRequest("Updated", "updated@test.com");

        recordService.update(TEST_UUID, request);

        verify(emergencyContactRepository).deleteByRecordId(1L);
        verify(certificationRepository).deleteByRecordId(1L);
    }

    @Test
    void search_blankParams_treatedAsNull() {
        when(recordRepository.search(null, null, null, null, null))
                .thenReturn(Collections.emptyList());

        List<RecordSummary> result = recordService.search("", "  ", "", null, "");

        assertThat(result).isEmpty();
        verify(recordRepository).search(null, null, null, null, null);
    }

    @Test
    void create_withCompensation_savesCompensation() {
        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("Test").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .compensation(CompensationRequest.builder()
                        .baseSalary(new java.math.BigDecimal("100000"))
                        .payFrequency("annual")
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .build())
                .build();

        when(recordRepository.insert(any(Record.class))).thenReturn(10L);
        Record saved = buildRecord(10L, TEST_UUID, "Test", "t@t.com");
        when(recordRepository.findById(10L)).thenReturn(Optional.of(saved));
        when(compensationRepository.findByRecordId(10L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());

        recordService.create(request);

        verify(compensationRepository).insert(any(Compensation.class));
    }

    @Test
    void create_withoutCompensation_skipsCompensation() {
        RecordRequest request = buildRequest("Test", "t@t.com");

        when(recordRepository.insert(any(Record.class))).thenReturn(10L);
        Record saved = buildRecord(10L, TEST_UUID, "Test", "t@t.com");
        when(recordRepository.findById(10L)).thenReturn(Optional.of(saved));
        when(compensationRepository.findByRecordId(10L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(10L)).thenReturn(Collections.emptyList());

        recordService.create(request);

        verify(compensationRepository, never()).insert(any(Compensation.class));
    }

    @Test
    void update_withCompensation_existingRow_updates() {
        Record existing = buildRecord(1L, TEST_UUID, "Old", "old@test.com");
        Record updated = buildRecord(1L, TEST_UUID, "Updated", "updated@test.com");

        when(recordRepository.findByUuid(TEST_UUID))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(updated));
        when(recordRepository.update(eq(TEST_UUID), any(Record.class))).thenReturn(1);
        when(compensationRepository.findByRecordId(1L))
                .thenReturn(Optional.of(Compensation.builder().id(1L).recordId(1L).build()));
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("Updated").email("updated@test.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .compensation(CompensationRequest.builder()
                        .baseSalary(new java.math.BigDecimal("120000"))
                        .payFrequency("annual")
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .build())
                .build();

        recordService.update(TEST_UUID, request);

        verify(compensationRepository).update(any(Compensation.class));
        verify(compensationRepository, never()).insert(any(Compensation.class));
    }

    @Test
    void update_withCompensation_noExistingRow_inserts() {
        Record existing = buildRecord(1L, TEST_UUID, "Old", "old@test.com");
        Record updated = buildRecord(1L, TEST_UUID, "Updated", "updated@test.com");

        when(recordRepository.findByUuid(TEST_UUID))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(updated));
        when(recordRepository.update(eq(TEST_UUID), any(Record.class))).thenReturn(1);
        // First call for syncCompensation check, second for loadRelations
        when(compensationRepository.findByRecordId(1L))
                .thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("Updated").email("updated@test.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .compensation(CompensationRequest.builder()
                        .baseSalary(new java.math.BigDecimal("80000"))
                        .payFrequency("monthly")
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .build())
                .build();

        recordService.update(TEST_UUID, request);

        verify(compensationRepository).insert(any(Compensation.class));
        verify(compensationRepository, never()).update(any(Compensation.class));
    }

    @Test
    void update_withNullCompensation_deletesExisting() {
        Record existing = buildRecord(1L, TEST_UUID, "Old", "old@test.com");
        Record updated = buildRecord(1L, TEST_UUID, "Updated", "updated@test.com");

        when(recordRepository.findByUuid(TEST_UUID))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(updated));
        when(recordRepository.update(eq(TEST_UUID), any(Record.class))).thenReturn(1);
        when(compensationRepository.findByRecordId(1L)).thenReturn(Optional.empty());
        when(emergencyContactRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        RecordRequest request = buildRequest("Updated", "updated@test.com");

        recordService.update(TEST_UUID, request);

        verify(compensationRepository).deleteByRecordId(1L);
    }
}
