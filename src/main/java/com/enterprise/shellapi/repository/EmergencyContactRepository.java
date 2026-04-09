package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.EmergencyContact;
import com.enterprise.shellapi.util.SqlQueryLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmergencyContactRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueryLoader;

    private static final RowMapper<EmergencyContact> ROW_MAPPER = (rs, rowNum) -> EmergencyContact.builder()
            .id(rs.getLong("id"))
            .recordId(rs.getLong("record_id"))
            .name(rs.getString("name"))
            .relationship(rs.getString("relationship"))
            .phone(rs.getString("phone"))
            .email(rs.getString("email"))
            .isPrimary(rs.getBoolean("is_primary"))
            .build();

    public List<EmergencyContact> findByRecordId(Long recordId) {
        String sql = sqlQueryLoader.getQuery("emergencyContacts", "findByRecordId");
        return jdbcTemplate.query(sql, new MapSqlParameterSource("recordId", recordId), ROW_MAPPER);
    }

    public List<EmergencyContact> findByRecordIds(List<Long> recordIds) {
        if (recordIds.isEmpty()) return List.of();
        String sql = sqlQueryLoader.getQuery("emergencyContacts", "findByRecordIds");
        return jdbcTemplate.query(sql, new MapSqlParameterSource("recordIds", recordIds), ROW_MAPPER);
    }

    public Long insert(EmergencyContact contact) {
        String sql = sqlQueryLoader.getQuery("emergencyContacts", "insert");
        MapSqlParameterSource params = buildParams(contact);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    public void update(EmergencyContact contact) {
        String sql = sqlQueryLoader.getQuery("emergencyContacts", "update");
        MapSqlParameterSource params = buildParams(contact);
        params.addValue("id", contact.getId());
        jdbcTemplate.update(sql, params);
    }

    public void deleteByRecordId(Long recordId) {
        String sql = sqlQueryLoader.getQuery("emergencyContacts", "deleteByRecordId");
        jdbcTemplate.update(sql, new MapSqlParameterSource("recordId", recordId));
    }

    public void deleteByRecordIdExcluding(Long recordId, List<Long> idsToKeep) {
        if (idsToKeep.isEmpty()) {
            deleteByRecordId(recordId);
            return;
        }
        String sql = sqlQueryLoader.getQuery("emergencyContacts", "deleteByIds");
        jdbcTemplate.update(sql, new MapSqlParameterSource("recordId", recordId).addValue("ids", idsToKeep));
    }

    private MapSqlParameterSource buildParams(EmergencyContact contact) {
        return new MapSqlParameterSource()
                .addValue("recordId", contact.getRecordId())
                .addValue("name", contact.getName())
                .addValue("relationship", contact.getRelationship())
                .addValue("phone", contact.getPhone())
                .addValue("email", contact.getEmail())
                .addValue("isPrimary", contact.getIsPrimary());
    }
}
