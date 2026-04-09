package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.*;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final CertificationRepository certificationRepository;

    public PagedResponse<Record> search(String name, String email, String department,
                                         String status, String address, int page, int size) {
        int offset = page * size;
        String nameParam = blankToNull(name);
        String emailParam = blankToNull(email);
        String deptParam = blankToNull(department);
        String statusParam = blankToNull(status);
        String addressParam = blankToNull(address);

        List<Record> records = recordRepository.search(nameParam, emailParam, deptParam,
                statusParam, addressParam, size, offset);
        long total = recordRepository.count(nameParam, emailParam, deptParam, statusParam, addressParam);

        records.forEach(this::loadRelations);

        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;

        return PagedResponse.<Record>builder()
                .content(records)
                .totalElements(total)
                .totalPages(totalPages)
                .size(size)
                .number(page)
                .build();
    }

    public Record findById(Long id) {
        Record record = recordRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(id));
        loadRelations(record);
        return record;
    }

    @Transactional
    public Record create(RecordRequest request) {
        Record record = mapToRecord(request);
        Long id = recordRepository.insert(record);
        saveRelations(id, request);
        return findById(id);
    }

    @Transactional
    public Record update(Long id, RecordRequest request) {
        recordRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(id));

        Record record = mapToRecord(request);
        recordRepository.update(id, record);

        emergencyContactRepository.deleteByRecordId(id);
        certificationRepository.deleteByRecordId(id);
        saveRelations(id, request);

        return findById(id);
    }

    @Transactional
    public void delete(Long id) {
        recordRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(id));
        recordRepository.delete(id);
    }

    private void loadRelations(Record record) {
        record.setEmergencyContacts(emergencyContactRepository.findByRecordId(record.getId()));
        record.setCertifications(certificationRepository.findByRecordId(record.getId()));
    }

    private void saveRelations(Long recordId, RecordRequest request) {
        if (request.getEmergencyContacts() != null) {
            for (EmergencyContactRequest ecReq : request.getEmergencyContacts()) {
                EmergencyContact contact = EmergencyContact.builder()
                        .recordId(recordId)
                        .name(ecReq.getName())
                        .relationship(ecReq.getRelationship())
                        .phone(ecReq.getPhone())
                        .email(ecReq.getEmail())
                        .isPrimary(ecReq.getIsPrimary() != null ? ecReq.getIsPrimary() : false)
                        .build();
                emergencyContactRepository.insert(contact);
            }
        }
        if (request.getCertifications() != null) {
            for (CertificationRequest certReq : request.getCertifications()) {
                Certification cert = Certification.builder()
                        .recordId(recordId)
                        .name(certReq.getName())
                        .issuingBody(certReq.getIssuingBody())
                        .issueDate(certReq.getIssueDate())
                        .expiryDate(certReq.getExpiryDate())
                        .credentialId(certReq.getCredentialId())
                        .build();
                certificationRepository.insert(cert);
            }
        }
    }

    private Record mapToRecord(RecordRequest request) {
        PersonalInfoRequest pi = request.getPersonalInfo();
        WorkInfoRequest wi = request.getWorkInfo();
        PreferencesRequest pref = request.getPreferences();

        return Record.builder()
                .personalInfo(PersonalInfo.builder()
                        .name(pi.getName())
                        .email(pi.getEmail())
                        .phone(pi.getPhone())
                        .address(pi.getAddress())
                        .dateOfBirth(pi.getDateOfBirth())
                        .ssn(pi.getSsn())
                        .bio(pi.getBio())
                        .build())
                .workInfo(WorkInfo.builder()
                        .jobTitle(wi.getJobTitle())
                        .manager(wi.getManager())
                        .department(wi.getDepartment())
                        .status(wi.getStatus() != null ? wi.getStatus() : "active")
                        .startDate(wi.getStartDate())
                        .employmentType(wi.getEmploymentType())
                        .build())
                .preferences(pref != null ? Preferences.builder()
                        .remoteEligible(pref.getRemoteEligible() != null ? pref.getRemoteEligible() : false)
                        .notificationsEnabled(pref.getNotificationsEnabled() != null ? pref.getNotificationsEnabled() : true)
                        .notificationChannels(pref.getNotificationChannels())
                        .accessLevel(pref.getAccessLevel() != null ? pref.getAccessLevel() : "standard")
                        .notes(pref.getNotes())
                        .build() : Preferences.builder()
                        .remoteEligible(false)
                        .notificationsEnabled(true)
                        .accessLevel("standard")
                        .build())
                .build();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
