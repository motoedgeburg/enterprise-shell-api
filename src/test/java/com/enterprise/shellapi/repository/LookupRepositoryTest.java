package com.enterprise.shellapi.repository;

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
class LookupRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SqlQueryLoader sqlQueryLoader;

    private LookupRepository lookupRepository;

    @BeforeEach
    void setUp() {
        lookupRepository = new LookupRepository(jdbcTemplate, sqlQueryLoader);
    }

    @Test
    void findDepartments_returnsSeededDepartments() {
        List<String> departments = lookupRepository.findDepartments();
        assertThat(departments).contains("Engineering", "Human Resources", "Marketing");
        assertThat(departments).hasSize(8);
    }

    @Test
    void findStatuses_returnsSeededStatuses() {
        List<String> statuses = lookupRepository.findStatuses();
        assertThat(statuses).contains("active", "inactive", "on-leave", "terminated");
    }

    @Test
    void findEmploymentTypes_returnsSeededTypes() {
        List<String> types = lookupRepository.findEmploymentTypes();
        assertThat(types).contains("full-time", "part-time", "contract", "intern");
    }

    @Test
    void findNotificationChannels_returnsSeededChannels() {
        List<String> channels = lookupRepository.findNotificationChannels();
        assertThat(channels).contains("email", "slack", "sms", "push");
    }

    @Test
    void findAccessLevels_returnsSeededLevels() {
        List<String> levels = lookupRepository.findAccessLevels();
        assertThat(levels).contains("standard", "elevated", "admin", "restricted");
    }

    @Test
    void findRelationships_returnsDistinctRelationships() {
        List<String> relationships = lookupRepository.findRelationships();
        assertThat(relationships).isNotEmpty();
        assertThat(relationships).contains("Spouse");
    }
}
