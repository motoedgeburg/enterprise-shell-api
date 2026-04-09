package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.PersonalInfo;
import com.enterprise.shellapi.model.Preferences;
import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.model.WorkInfo;
import com.enterprise.shellapi.util.SqlQueryLoader;
import com.enterprise.shellapi.util.SsnEncryptor;
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
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RecordRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueryLoader;
    private final SsnEncryptor ssnEncryptor;

    private RowMapper<Record> rowMapper() {
        return (rs, rowNum) -> Record.builder()
                .id(rs.getLong("id"))
                .uuid(rs.getString("uuid"))
                .personalInfo(PersonalInfo.builder()
                        .name(rs.getString("name"))
                        .email(rs.getString("email"))
                        .phone(rs.getString("phone"))
                        .address(rs.getString("address"))
                        .dateOfBirth(getLocalDate(rs, "date_of_birth"))
                        .ssn(ssnEncryptor.decrypt(rs.getString("ssn")))
                        .bio(rs.getString("bio"))
                        .build())
                .workInfo(WorkInfo.builder()
                        .jobTitle(rs.getString("job_title"))
                        .manager(rs.getString("manager"))
                        .department(rs.getString("department"))
                        .status(rs.getString("status"))
                        .startDate(getLocalDate(rs, "start_date"))
                        .employmentType(rs.getString("employment_type"))
                        .build())
                .preferences(Preferences.builder()
                        .remoteEligible(rs.getBoolean("remote_eligible"))
                        .notificationsEnabled(rs.getBoolean("notifications_enabled"))
                        .notificationChannels(parseChannels(rs.getString("notification_channels")))
                        .accessLevel(rs.getString("access_level"))
                        .notes(rs.getString("notes"))
                        .build())
                .createdAt(getTimestamp(rs, "created_at"))
                .updatedAt(getTimestamp(rs, "updated_at"))
                .build();
    }

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
        return jdbcTemplate.query(sql, params, rowMapper());
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
        List<Record> results = jdbcTemplate.query(sql, params, rowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Record> findByUuid(String uuid) {
        String sql = sqlQueryLoader.getQuery("records", "findByUuid");
        MapSqlParameterSource params = new MapSqlParameterSource("uuid", uuid);
        List<Record> results = jdbcTemplate.query(sql, params, rowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Long insert(Record record) {
        String sql = sqlQueryLoader.getQuery("records", "insert");
        MapSqlParameterSource params = buildParams(record);
        params.addValue("uuid", UUID.randomUUID().toString());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    public int update(String uuid, Record record) {
        String sql = sqlQueryLoader.getQuery("records", "update");
        MapSqlParameterSource params = buildParams(record);
        params.addValue("uuid", uuid);
        return jdbcTemplate.update(sql, params);
    }

    public int delete(String uuid) {
        String sql = sqlQueryLoader.getQuery("records", "delete");
        return jdbcTemplate.update(sql, new MapSqlParameterSource("uuid", uuid));
    }

    private MapSqlParameterSource buildParams(Record record) {
        PersonalInfo pi = record.getPersonalInfo();
        WorkInfo wi = record.getWorkInfo();
        Preferences pref = record.getPreferences();

        return new MapSqlParameterSource()
                .addValue("name", pi.getName())
                .addValue("email", pi.getEmail())
                .addValue("phone", pi.getPhone())
                .addValue("address", pi.getAddress())
                .addValue("dateOfBirth", pi.getDateOfBirth())
                .addValue("ssn", ssnEncryptor.encrypt(pi.getSsn()))
                .addValue("bio", pi.getBio())
                .addValue("jobTitle", wi.getJobTitle())
                .addValue("manager", wi.getManager())
                .addValue("department", wi.getDepartment())
                .addValue("status", wi.getStatus())
                .addValue("startDate", wi.getStartDate())
                .addValue("employmentType", wi.getEmploymentType())
                .addValue("remoteEligible", pref.getRemoteEligible())
                .addValue("notificationsEnabled", pref.getNotificationsEnabled())
                .addValue("notificationChannels", pref.getNotificationChannels() != null
                        ? String.join(",", pref.getNotificationChannels()) : null)
                .addValue("accessLevel", pref.getAccessLevel())
                .addValue("notes", pref.getNotes());
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
