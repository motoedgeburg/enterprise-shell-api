package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.PersonalInfo;
import com.enterprise.shellapi.model.Preferences;
import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.model.WorkInfo;
import com.enterprise.shellapi.util.SqlQueryLoader;
import com.enterprise.shellapi.util.SsnEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import({SqlQueryLoader.class, SsnEncryptor.class})
class RecordRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SqlQueryLoader sqlQueryLoader;

    @Autowired
    private SsnEncryptor ssnEncryptor;

    private RecordRepository recordRepository;

    @BeforeEach
    void setUp() {
        recordRepository = new RecordRepository(jdbcTemplate, sqlQueryLoader, ssnEncryptor);
    }

    private Record buildRecord(String name, String email) {
        return Record.builder()
                .personalInfo(PersonalInfo.builder().name(name).email(email).build())
                .workInfo(WorkInfo.builder().status("active").build())
                .preferences(Preferences.builder()
                        .remoteEligible(false)
                        .notificationsEnabled(true)
                        .accessLevel("standard")
                        .build())
                .build();
    }

    @Test
    void search_noFilters_returnsSeededRecords() {
        List<Record> records = recordRepository.search(null, null, null, null, null, 10, 0);
        assertThat(records).isNotEmpty();
        assertThat(records.size()).isLessThanOrEqualTo(10);
    }

    @Test
    void search_byName_filtersCorrectly() {
        List<Record> records = recordRepository.search("Alice", null, null, null, null, 10, 0);
        assertThat(records).allSatisfy(r ->
                assertThat(r.getPersonalInfo().getName().toLowerCase()).contains("alice"));
    }

    @Test
    void search_byDepartment_filtersCorrectly() {
        List<Record> records = recordRepository.search(null, null, "Engineering", null, null, 10, 0);
        assertThat(records).allSatisfy(r ->
                assertThat(r.getWorkInfo().getDepartment()).isEqualTo("Engineering"));
    }

    @Test
    void search_pagination_works() {
        List<Record> page1 = recordRepository.search(null, null, null, null, null, 3, 0);
        List<Record> page2 = recordRepository.search(null, null, null, null, null, 3, 3);

        assertThat(page1).hasSize(3);
        assertThat(page2).isNotEmpty();
        assertThat(page1.get(0).getId()).isNotEqualTo(page2.get(0).getId());
    }

    @Test
    void count_noFilters_returnsTotal() {
        long count = recordRepository.count(null, null, null, null, null);
        assertThat(count).isEqualTo(8); // 8 seeded records
    }

    @Test
    void findById_existingRecord_returnsRecord() {
        Optional<Record> result = recordRepository.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getPersonalInfo().getName()).isEqualTo("Alice Johnson");
    }

    @Test
    void findById_nonExistingRecord_returnsEmpty() {
        Optional<Record> result = recordRepository.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void insert_createsNewRecord() {
        Record record = Record.builder()
                .personalInfo(PersonalInfo.builder()
                        .name("Test Person")
                        .email("test@test.com")
                        .phone("(555) 123-4567")
                        .build())
                .workInfo(WorkInfo.builder()
                        .department("Engineering")
                        .status("active")
                        .startDate(LocalDate.of(2024, 1, 1))
                        .build())
                .preferences(Preferences.builder()
                        .remoteEligible(true)
                        .notificationsEnabled(true)
                        .accessLevel("standard")
                        .build())
                .build();

        Long id = recordRepository.insert(record);

        assertThat(id).isNotNull().isPositive();
        Optional<Record> saved = recordRepository.findById(id);
        assertThat(saved).isPresent();
        assertThat(saved.get().getPersonalInfo().getName()).isEqualTo("Test Person");
    }

    @Test
    void update_modifiesRecord() {
        Record record = Record.builder()
                .personalInfo(PersonalInfo.builder()
                        .name("Updated Name")
                        .email("alice.johnson@company.com")
                        .build())
                .workInfo(WorkInfo.builder()
                        .status("active")
                        .build())
                .preferences(Preferences.builder()
                        .remoteEligible(false)
                        .notificationsEnabled(true)
                        .accessLevel("elevated")
                        .build())
                .build();

        int rows = recordRepository.update(1L, record);

        assertThat(rows).isEqualTo(1);
        Optional<Record> updated = recordRepository.findById(1L);
        assertThat(updated).isPresent();
        assertThat(updated.get().getPersonalInfo().getName()).isEqualTo("Updated Name");
        assertThat(updated.get().getPreferences().getAccessLevel()).isEqualTo("elevated");
    }

    @Test
    void delete_removesRecord() {
        Record record = buildRecord("To Delete", "delete-me@test.com");
        Long id = recordRepository.insert(record);

        int rows = recordRepository.delete(id);

        assertThat(rows).isEqualTo(1);
        assertThat(recordRepository.findById(id)).isEmpty();
    }
}
