package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.util.SqlQueryLoader;
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
@Import(SqlQueryLoader.class)
class RecordRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SqlQueryLoader sqlQueryLoader;

    private RecordRepository recordRepository;

    @BeforeEach
    void setUp() {
        recordRepository = new RecordRepository(jdbcTemplate, sqlQueryLoader);
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
                assertThat(r.getName().toLowerCase()).contains("alice"));
    }

    @Test
    void search_byDepartment_filtersCorrectly() {
        List<Record> records = recordRepository.search(null, null, "Engineering", null, null, 10, 0);
        assertThat(records).allSatisfy(r ->
                assertThat(r.getDepartment()).isEqualTo("Engineering"));
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
        assertThat(result.get().getName()).isEqualTo("Alice Johnson");
    }

    @Test
    void findById_nonExistingRecord_returnsEmpty() {
        Optional<Record> result = recordRepository.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void insert_createsNewRecord() {
        Record record = Record.builder()
                .name("Test Person")
                .email("test@test.com")
                .phone("(555) 123-4567")
                .department("Engineering")
                .status("active")
                .remoteEligible(true)
                .notificationsEnabled(true)
                .accessLevel("standard")
                .startDate(LocalDate.of(2024, 1, 1))
                .build();

        Long id = recordRepository.insert(record);

        assertThat(id).isNotNull().isPositive();
        Optional<Record> saved = recordRepository.findById(id);
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("Test Person");
    }

    @Test
    void update_modifiesRecord() {
        Record record = Record.builder()
                .name("Updated Name")
                .email("alice.johnson@company.com")
                .status("active")
                .remoteEligible(false)
                .notificationsEnabled(true)
                .accessLevel("elevated")
                .build();

        int rows = recordRepository.update(1L, record);

        assertThat(rows).isEqualTo(1);
        Optional<Record> updated = recordRepository.findById(1L);
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Name");
        assertThat(updated.get().getAccessLevel()).isEqualTo("elevated");
    }

    @Test
    void delete_removesRecord() {
        // Insert then delete to avoid breaking other tests
        Record record = Record.builder()
                .name("To Delete")
                .email("delete-me@test.com")
                .status("active")
                .remoteEligible(false)
                .notificationsEnabled(false)
                .accessLevel("standard")
                .build();
        Long id = recordRepository.insert(record);

        int rows = recordRepository.delete(id);

        assertThat(rows).isEqualTo(1);
        assertThat(recordRepository.findById(id)).isEmpty();
    }
}
