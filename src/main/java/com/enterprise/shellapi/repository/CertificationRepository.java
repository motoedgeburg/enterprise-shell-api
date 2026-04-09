package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.Certification;
import com.enterprise.shellapi.util.SqlQueryLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CertificationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueryLoader;

    private static final RowMapper<Certification> ROW_MAPPER = (rs, rowNum) -> {
        Date issueDate = rs.getDate("issue_date");
        Date expiryDate = rs.getDate("expiry_date");
        return Certification.builder()
                .id(rs.getLong("id"))
                .recordId(rs.getLong("record_id"))
                .name(rs.getString("name"))
                .issuingBody(rs.getString("issuing_body"))
                .issueDate(issueDate != null ? issueDate.toLocalDate() : null)
                .expiryDate(expiryDate != null ? expiryDate.toLocalDate() : null)
                .credentialId(rs.getString("credential_id"))
                .build();
    };

    public List<Certification> findByRecordId(Long recordId) {
        String sql = sqlQueryLoader.getQuery("certifications", "findByRecordId");
        return jdbcTemplate.query(sql, new MapSqlParameterSource("recordId", recordId), ROW_MAPPER);
    }

    public List<Certification> findByRecordIds(List<Long> recordIds) {
        if (recordIds.isEmpty()) return List.of();
        String sql = sqlQueryLoader.getQuery("certifications", "findByRecordIds");
        return jdbcTemplate.query(sql, new MapSqlParameterSource("recordIds", recordIds), ROW_MAPPER);
    }

    public Long insert(Certification certification) {
        String sql = sqlQueryLoader.getQuery("certifications", "insert");
        MapSqlParameterSource params = buildParams(certification);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    public void update(Certification certification) {
        String sql = sqlQueryLoader.getQuery("certifications", "update");
        MapSqlParameterSource params = buildParams(certification);
        params.addValue("id", certification.getId());
        jdbcTemplate.update(sql, params);
    }

    public void deleteByRecordId(Long recordId) {
        String sql = sqlQueryLoader.getQuery("certifications", "deleteByRecordId");
        jdbcTemplate.update(sql, new MapSqlParameterSource("recordId", recordId));
    }

    public void deleteByRecordIdExcluding(Long recordId, List<Long> idsToKeep) {
        if (idsToKeep.isEmpty()) {
            deleteByRecordId(recordId);
            return;
        }
        String sql = sqlQueryLoader.getQuery("certifications", "deleteByIds");
        jdbcTemplate.update(sql, new MapSqlParameterSource("recordId", recordId).addValue("ids", idsToKeep));
    }

    private MapSqlParameterSource buildParams(Certification certification) {
        return new MapSqlParameterSource()
                .addValue("recordId", certification.getRecordId())
                .addValue("name", certification.getName())
                .addValue("issuingBody", certification.getIssuingBody())
                .addValue("issueDate", certification.getIssueDate())
                .addValue("expiryDate", certification.getExpiryDate())
                .addValue("credentialId", certification.getCredentialId());
    }
}
