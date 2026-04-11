package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.EmergencyContact;
import com.enterprise.shellapi.util.SqlQueryLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import(SqlQueryLoader.class)
class EmergencyContactRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SqlQueryLoader sqlQueryLoader;

    private EmergencyContactRepository emergencyContactRepository;

    @BeforeEach
    void setUp() {
        emergencyContactRepository = new EmergencyContactRepository(jdbcTemplate, sqlQueryLoader);
    }

    @Test
    void findByRecordId_returnsContacts() {
        List<EmergencyContact> contacts = emergencyContactRepository.findByRecordId(1L);
        assertThat(contacts).hasSize(1);
        assertThat(contacts.get(0).getName()).isEqualTo("Michael Johnson");
        assertThat(contacts.get(0).getIsPrimary()).isTrue();
    }

    @Test
    void findByRecordId_multipleContacts() {
        List<EmergencyContact> contacts = emergencyContactRepository.findByRecordId(2L);
        assertThat(contacts).hasSize(2);
        // Primary should come first
        assertThat(contacts.get(0).getIsPrimary()).isTrue();
    }

    @Test
    void insert_addsContact() {
        EmergencyContact contact = EmergencyContact.builder()
                .recordId(1L)
                .name("New Contact")
                .relationship("Friend")
                .phone("(555) 000-0000")
                .email("friend@test.com")
                .isPrimary(false)
                .build();

        emergencyContactRepository.insert(contact);

        List<EmergencyContact> contacts = emergencyContactRepository.findByRecordId(1L);
        assertThat(contacts).hasSize(2);
    }

    @Test
    void findByRecordId_noResults_returnsEmptyList() {
        List<EmergencyContact> contacts = emergencyContactRepository.findByRecordId(999L);
        assertThat(contacts).isEmpty();
    }

    @Test
    void findByRecordIds_returnsAcrossRecords() {
        List<EmergencyContact> contacts = emergencyContactRepository.findByRecordIds(List.of(1L, 2L));
        assertThat(contacts).isNotEmpty();
        assertThat(contacts).allSatisfy(c ->
                assertThat(c.getRecordId()).isIn(1L, 2L));
    }

    @Test
    void findByRecordIds_emptyList_returnsEmpty() {
        List<EmergencyContact> contacts = emergencyContactRepository.findByRecordIds(List.of());
        assertThat(contacts).isEmpty();
    }

    @Test
    void update_modifiesContact() {
        List<EmergencyContact> existing = emergencyContactRepository.findByRecordId(1L);
        assertThat(existing).isNotEmpty();
        EmergencyContact contact = existing.get(0);

        EmergencyContact updated = EmergencyContact.builder()
                .id(contact.getId())
                .recordId(contact.getRecordId())
                .name("Updated Name")
                .relationship(contact.getRelationship())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .isPrimary(contact.getIsPrimary())
                .build();

        emergencyContactRepository.update(updated);

        List<EmergencyContact> afterUpdate = emergencyContactRepository.findByRecordId(1L);
        assertThat(afterUpdate).anyMatch(c -> c.getName().equals("Updated Name"));
    }

    @Test
    void deleteByRecordId_removesAllContacts() {
        emergencyContactRepository.deleteByRecordId(2L);

        List<EmergencyContact> contacts = emergencyContactRepository.findByRecordId(2L);
        assertThat(contacts).isEmpty();
    }

    @Test
    void deleteByRecordIdExcluding_keepsSpecifiedIds() {
        List<EmergencyContact> existing = emergencyContactRepository.findByRecordId(2L);
        assertThat(existing).hasSizeGreaterThanOrEqualTo(2);
        Long keepId = existing.get(0).getId();

        emergencyContactRepository.deleteByRecordIdExcluding(2L, List.of(keepId));

        List<EmergencyContact> remaining = emergencyContactRepository.findByRecordId(2L);
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getId()).isEqualTo(keepId);
    }

    @Test
    void deleteByRecordIdExcluding_emptyKeepList_deletesAll() {
        assertThat(emergencyContactRepository.findByRecordId(2L)).isNotEmpty();

        emergencyContactRepository.deleteByRecordIdExcluding(2L, List.of());

        assertThat(emergencyContactRepository.findByRecordId(2L)).isEmpty();
    }
}
