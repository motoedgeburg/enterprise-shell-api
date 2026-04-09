package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.util.SqlQueryLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RecordRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueryLoader;

    private static final RowMapper<Record> ROW_MAPPER = (rs, rowNum) -> Record.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .email(rs.getString("email"))
            .phone(rs.getString("phone"))
            .address(rs.getString("address"))
            .dateOfBirth(getLocalDate(rs, "date_of_birth"))
            .ssn(rs.getString("ssn"))
            .bio(rs.getString("bio"))
            .department(rs.getString("department"))
            .jobTitle(rs.getString("job_title"))
            .employmentType(rs.getString("employment_type"))
            .startDate(getLocalDate(rs, "start_date"))
            .manager(rs.getString("manager"))
            .status(rs.getString("status"))
            .remoteEligible(rs.getBoolean("remote_eligible"))
            .notificationsEnabled(rs.getBoolean("notifications_enabled"))
            .notificationChannels(parseChannels(rs.getString("notification_channels")))
            .accessLevel(rs.getString("access_level"))
            .notes(rs.getString("notes"))
            .createdAt(getTimestamp(rs, "created_at"))
            .build();

    public List<Record> search(String name, String email, String department, String status,
                                String address, int limit, int offset) {
        String sql = sqlQueryLoader.getQuery("records", "search");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("email", email)
                .addValue("department", department)
                .addValue("status", status)
                .addValue("address", address)
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public long count(String name, String email, String department, String status, String address) {
        String sql = sqlQueryLoader.getQuery("records", "count");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("email", email)
                .addValue("department", department)
                .addValue("status", status)
                .addValue("address", address);
        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0;
    }

    public Optional<Record> findById(Long id) {
        String sql = sqlQueryLoader.getQuery("records", "findById");
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        List<Record> results = jdbcTemplate.query(sql, params, ROW_MAPPER);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Long insert(Record record) {
        String sql = sqlQueryLoader.getQuery("records", "insert");
        MapSqlParameterSource params = buildParams(record);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    public int update(Long id, Record record) {
        String sql = sqlQueryLoader.getQuery("records", "update");
        MapSqlParameterSource params = buildParams(record);
        params.addValue("id", id);
        return jdbcTemplate.update(sql, params);
    }

    public int delete(Long id) {
        String sql = sqlQueryLoader.getQuery("records", "delete");
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    private MapSqlParameterSource buildParams(Record record) {
        return new MapSqlParameterSource()
                .addValue("name", record.getName())
                .addValue("email", record.getEmail())
                .addValue("phone", record.getPhone())
                .addValue("address", record.getAddress())
                .addValue("dateOfBirth", record.getDateOfBirth())
                .addValue("ssn", record.getSsn())
                .addValue("bio", record.getBio())
                .addValue("department", record.getDepartment())
                .addValue("jobTitle", record.getJobTitle())
                .addValue("employmentType", record.getEmploymentType())
                .addValue("startDate", record.getStartDate())
                .addValue("manager", record.getManager())
                .addValue("status", record.getStatus())
                .addValue("remoteEligible", record.getRemoteEligible())
                .addValue("notificationsEnabled", record.getNotificationsEnabled())
                .addValue("notificationChannels", record.getNotificationChannels() != null
                        ? String.join(",", record.getNotificationChannels()) : null)
                .addValue("accessLevel", record.getAccessLevel())
                .addValue("notes", record.getNotes());
    }

    private static List<String> parseChannels(String channels) {
        if (channels == null || channels.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(channels.split(","));
    }

    private static LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        java.sql.Date date = rs.getDate(column);
        return date != null ? date.toLocalDate() : null;
    }

    private static java.time.LocalDateTime getTimestamp(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
