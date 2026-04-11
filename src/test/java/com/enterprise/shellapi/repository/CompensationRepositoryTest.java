package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.Compensation;
import com.enterprise.shellapi.util.SqlQueryLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import(SqlQueryLoader.class)
class CompensationRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SqlQueryLoader sqlQueryLoader;

    private CompensationRepository compensationRepository;

    @BeforeEach
    void setUp() {
        compensationRepository = new CompensationRepository(jdbcTemplate, sqlQueryLoader);
    }

    @Test
    void findByRecordId_existingCompensation_returnsIt() {
        Optional<Compensation> result = compensationRepository.findByRecordId(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getBaseSalary()).isNotNull();
        assertThat(result.get().getPayFrequency()).isEqualTo("annual");
        assertThat(result.get().getEffectiveDate()).isNotNull();
        assertThat(result.get().getOvertimeEligible()).isNotNull();
    }

    @Test
    void findByRecordId_noCompensation_returnsEmpty() {
        Optional<Compensation> result = compensationRepository.findByRecordId(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void insert_addsCompensation() {
        // First delete existing compensation for record 1 to avoid unique constraint
        compensationRepository.deleteByRecordId(1L);

        Compensation comp = Compensation.builder()
                .recordId(1L)
                .baseSalary(new BigDecimal("95000.00"))
                .payFrequency("monthly")
                .bonusTarget(new BigDecimal("10.00"))
                .stockOptions(500)
                .effectiveDate(LocalDate.of(2024, 6, 1))
                .overtimeEligible(false)
                .build();

        Long id = compensationRepository.insert(comp);
        assertThat(id).isNotNull().isPositive();

        Optional<Compensation> saved = compensationRepository.findByRecordId(1L);
        assertThat(saved).isPresent();
        assertThat(saved.get().getBaseSalary()).isEqualByComparingTo(new BigDecimal("95000.00"));
        assertThat(saved.get().getPayFrequency()).isEqualTo("monthly");
        assertThat(saved.get().getBonusTarget()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(saved.get().getStockOptions()).isEqualTo(500);
        assertThat(saved.get().getOvertimeEligible()).isFalse();
    }

    @Test
    void insert_nullOptionalFields_succeeds() {
        compensationRepository.deleteByRecordId(1L);

        Compensation comp = Compensation.builder()
                .recordId(1L)
                .baseSalary(new BigDecimal("50000.00"))
                .payFrequency("weekly")
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .overtimeEligible(true)
                .build();

        Long id = compensationRepository.insert(comp);
        assertThat(id).isNotNull().isPositive();

        Optional<Compensation> saved = compensationRepository.findByRecordId(1L);
        assertThat(saved).isPresent();
        assertThat(saved.get().getBonusTarget()).isNull();
        assertThat(saved.get().getStockOptions()).isNull();
    }

    @Test
    void update_modifiesCompensation() {
        Optional<Compensation> existing = compensationRepository.findByRecordId(1L);
        assertThat(existing).isPresent();

        Compensation updated = Compensation.builder()
                .recordId(1L)
                .baseSalary(new BigDecimal("150000.00"))
                .payFrequency("bi-weekly")
                .bonusTarget(new BigDecimal("20.00"))
                .stockOptions(3000)
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .overtimeEligible(true)
                .build();

        compensationRepository.update(updated);

        Optional<Compensation> after = compensationRepository.findByRecordId(1L);
        assertThat(after).isPresent();
        assertThat(after.get().getBaseSalary()).isEqualByComparingTo(new BigDecimal("150000.00"));
        assertThat(after.get().getPayFrequency()).isEqualTo("bi-weekly");
        assertThat(after.get().getOvertimeEligible()).isTrue();
    }

    @Test
    void deleteByRecordId_removesCompensation() {
        assertThat(compensationRepository.findByRecordId(1L)).isPresent();

        compensationRepository.deleteByRecordId(1L);

        assertThat(compensationRepository.findByRecordId(1L)).isEmpty();
    }
}
