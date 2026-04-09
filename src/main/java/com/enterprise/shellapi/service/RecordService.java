package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.CertificationRequest;
import com.enterprise.shellapi.dto.EmergencyContactRequest;
import com.enterprise.shellapi.dto.PersonalInfoRequest;
import com.enterprise.shellapi.dto.PreferencesRequest;
import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.dto.RecordSummary;
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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final CertificationRepository certificationRepository;

    public List<RecordSummary> search(String name, String email, String department,
                                       String status, String address) {
        return recordRepository.search(
                blankToNull(name), blankToNull(email), blankToNull(department),
                blankToNull(status), blankToNull(address));
    }

    public Record findByUuid(String uuid) {
        Record record = recordRepository.findByUuid(uuid)
                .orElseThrow(() -> new RecordNotFoundException(uuid));
        loadRelations(record);

        return record;
    }

    @Transactional
    public Record create(RecordRequest request) {
        Record record = mapToRecord(request);
        Long id = recordRepository.insert(record);
        saveRelations(id, request);
        Record saved = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Record not found after insert"));
        loadRelations(saved);

        return saved;
    }

    @Transactional
    public Record update(String uuid, RecordRequest request) {
        Record existing = recordRepository.findByUuid(uuid)
                .orElseThrow(() -> new RecordNotFoundException(uuid));

        Record record = mapToRecord(request);
        recordRepository.update(uuid, record);

        Long internalId = existing.getId();
        syncEmergencyContacts(internalId, request.getEmergencyContacts());
        syncCertifications(internalId, request.getCertifications());

        return findByUuid(uuid);
    }

    @Transactional
    public void delete(String uuid) {
        recordRepository.findByUuid(uuid)
                .orElseThrow(() -> new RecordNotFoundException(uuid));
        recordRepository.delete(uuid);
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

    private void syncEmergencyContacts(Long recordId, List<EmergencyContactRequest> requests) {
        if (requests == null) {
            emergencyContactRepository.deleteByRecordId(recordId);
            return;
        }

        List<Long> keepIds = new ArrayList<>();
        for (EmergencyContactRequest ecReq : requests) {
            EmergencyContact contact = EmergencyContact.builder()
                    .recordId(recordId)
                    .name(ecReq.getName())
                    .relationship(ecReq.getRelationship())
                    .phone(ecReq.getPhone())
                    .email(ecReq.getEmail())
                    .isPrimary(ecReq.getIsPrimary() != null ? ecReq.getIsPrimary() : false)
                    .build();

            if (ecReq.getId() != null) {
                contact.setId(ecReq.getId());
                emergencyContactRepository.update(contact);
                keepIds.add(ecReq.getId());
            } else {
                Long newId = emergencyContactRepository.insert(contact);
                keepIds.add(newId);
            }
        }

        emergencyContactRepository.deleteByRecordIdExcluding(recordId, keepIds);
    }

    private void syncCertifications(Long recordId, List<CertificationRequest> requests) {
        if (requests == null) {
            certificationRepository.deleteByRecordId(recordId);
            return;
        }

        List<Long> keepIds = new ArrayList<>();
        for (CertificationRequest certReq : requests) {
            Certification cert = Certification.builder()
                    .recordId(recordId)
                    .name(certReq.getName())
                    .issuingBody(certReq.getIssuingBody())
                    .issueDate(certReq.getIssueDate())
                    .expiryDate(certReq.getExpiryDate())
                    .credentialId(certReq.getCredentialId())
                    .build();

            if (certReq.getId() != null) {
                cert.setId(certReq.getId());
                certificationRepository.update(cert);
                keepIds.add(certReq.getId());
            } else {
                Long newId = certificationRepository.insert(cert);
                keepIds.add(newId);
            }
        }

        certificationRepository.deleteByRecordIdExcluding(recordId, keepIds);
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
