package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.util.SqlQueryLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LookupRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueryLoader;

    public List<String> findDepartments() {
        return queryForList("findDepartments");
    }

    public List<String> findStatuses() {
        return queryForList("findStatuses");
    }

    public List<String> findEmploymentTypes() {
        return queryForList("findEmploymentTypes");
    }

    public List<String> findNotificationChannels() {
        return queryForList("findNotificationChannels");
    }

    public List<String> findAccessLevels() {
        return queryForList("findAccessLevels");
    }

    public List<String> findRelationships() {
        return queryForList("findRelationships");
    }

    private List<String> queryForList(String queryName) {
        String sql = sqlQueryLoader.getQuery("lookups", queryName);
        return jdbcTemplate.queryForList(sql, Collections.emptyMap(), String.class);
    }
}
