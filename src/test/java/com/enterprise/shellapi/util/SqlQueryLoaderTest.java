package com.enterprise.shellapi.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@ActiveProfiles("test")
@Import(SqlQueryLoader.class)
class SqlQueryLoaderTest {

    @Autowired
    private SqlQueryLoader sqlQueryLoader;

    @Test
    void getQuery_validCategoryAndName_returnsQuery() {
        String query = sqlQueryLoader.getQuery("records", "findByUuid");
        assertThat(query).isNotBlank();
        assertThat(query).containsIgnoringCase("SELECT");
    }

    @Test
    void getQuery_invalidCategory_throwsIllegalArgument() {
        assertThatThrownBy(() -> sqlQueryLoader.getQuery("nonexistent", "findAll"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SQL query category not found: nonexistent");
    }

    @Test
    void getQuery_invalidName_throwsIllegalArgument() {
        assertThatThrownBy(() -> sqlQueryLoader.getQuery("records", "nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SQL query not found: records.nonexistent");
    }

    @Test
    void getQuery_trimmed_noLeadingTrailingWhitespace() {
        String query = sqlQueryLoader.getQuery("records", "insert");
        assertThat(query).doesNotStartWith(" ").doesNotEndWith(" ");
    }
}
