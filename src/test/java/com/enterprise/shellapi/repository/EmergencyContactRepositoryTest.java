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
    void deleteByRecordId_removesAllContacts() {
        emergencyContactRepository.deleteByRecordId(2L);

        List<EmergencyContact> contacts = emergencyContactRepository.findByRecordId(2L);
        assertThat(contacts).isEmpty();
    }
}
