package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.Compensation;
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
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CompensationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueryLoader;

    private static final RowMapper<Compensation> ROW_MAPPER = (rs, rowNum) -> {
        Date effectiveDate = rs.getDate("effective_date");
        return Compensation.builder()
                .id(rs.getLong("id"))
                .recordId(rs.getLong("record_id"))
                .baseSalary(rs.getBigDecimal("base_salary"))
                .payFrequency(rs.getString("pay_frequency"))
                .bonusTarget(rs.getBigDecimal("bonus_target"))
                .stockOptions((Integer) rs.getObject("stock_options"))
                .effectiveDate(effectiveDate != null ? effectiveDate.toLocalDate() : null)
                .overtimeEligible(rs.getBoolean("overtime_eligible"))
                .build();
    };

    public Optional<Compensation> findByRecordId(Long recordId) {
        String sql = sqlQueryLoader.getQuery("compensation", "findByRecordId");
        List<Compensation> results = jdbcTemplate.query(sql, new MapSqlParameterSource("recordId", recordId), ROW_MAPPER);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Long insert(Compensation compensation) {
        String sql = sqlQueryLoader.getQuery("compensation", "insert");
        MapSqlParameterSource params = buildParams(compensation);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    public void update(Compensation compensation) {
        String sql = sqlQueryLoader.getQuery("compensation", "update");
        MapSqlParameterSource params = buildParams(compensation);
        jdbcTemplate.update(sql, params);
    }

    public void deleteByRecordId(Long recordId) {
        String sql = sqlQueryLoader.getQuery("compensation", "deleteByRecordId");
        jdbcTemplate.update(sql, new MapSqlParameterSource("recordId", recordId));
    }

    private MapSqlParameterSource buildParams(Compensation compensation) {
        return new MapSqlParameterSource()
                .addValue("recordId", compensation.getRecordId())
                .addValue("baseSalary", compensation.getBaseSalary())
                .addValue("payFrequency", compensation.getPayFrequency())
                .addValue("bonusTarget", compensation.getBonusTarget())
                .addValue("stockOptions", compensation.getStockOptions())
                .addValue("effectiveDate", compensation.getEffectiveDate())
                .addValue("overtimeEligible", compensation.getOvertimeEligible());
    }
}
