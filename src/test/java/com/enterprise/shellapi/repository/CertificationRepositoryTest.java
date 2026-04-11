package com.enterprise.shellapi.repository;

import com.enterprise.shellapi.model.Certification;
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

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import(SqlQueryLoader.class)
class CertificationRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SqlQueryLoader sqlQueryLoader;

    private CertificationRepository certificationRepository;

    @BeforeEach
    void setUp() {
        certificationRepository = new CertificationRepository(jdbcTemplate, sqlQueryLoader);
    }

    @Test
    void findByRecordId_returnsCertifications() {
        List<Certification> certs = certificationRepository.findByRecordId(1L);
        assertThat(certs).isNotEmpty();
        assertThat(certs.get(0).getName()).isNotBlank();
        assertThat(certs.get(0).getRecordId()).isEqualTo(1L);
    }

    @Test
    void findByRecordId_noResults_returnsEmptyList() {
        List<Certification> certs = certificationRepository.findByRecordId(999L);
        assertThat(certs).isEmpty();
    }

    @Test
    void findByRecordIds_returnsAcrossRecords() {
        List<Certification> certs = certificationRepository.findByRecordIds(List.of(1L, 2L));
        assertThat(certs).isNotEmpty();
        assertThat(certs).allSatisfy(c ->
                assertThat(c.getRecordId()).isIn(1L, 2L));
    }

    @Test
    void findByRecordIds_emptyList_returnsEmpty() {
        List<Certification> certs = certificationRepository.findByRecordIds(List.of());
        assertThat(certs).isEmpty();
    }

    @Test
    void insert_addsCertification() {
        Certification cert = Certification.builder()
                .recordId(1L)
                .name("New Cert")
                .issuingBody("Test Org")
                .issueDate(LocalDate.of(2024, 1, 1))
                .expiryDate(LocalDate.of(2027, 1, 1))
                .credentialId("CERT-999")
                .build();

        Long id = certificationRepository.insert(cert);

        assertThat(id).isNotNull().isPositive();
        List<Certification> certs = certificationRepository.findByRecordId(1L);
        assertThat(certs).anyMatch(c -> c.getName().equals("New Cert"));
    }

    @Test
    void insert_nullOptionalFields_succeeds() {
        Certification cert = Certification.builder()
                .recordId(1L)
                .name("Minimal Cert")
                .issuingBody("Org")
                .issueDate(LocalDate.of(2024, 6, 1))
                .build();

        Long id = certificationRepository.insert(cert);
        assertThat(id).isNotNull().isPositive();
    }

    @Test
    void update_modifiesCertification() {
        List<Certification> existing = certificationRepository.findByRecordId(1L);
        assertThat(existing).isNotEmpty();
        Certification cert = existing.get(0);

        Certification updated = Certification.builder()
                .id(cert.getId())
                .recordId(cert.getRecordId())
                .name("Updated Cert Name")
                .issuingBody(cert.getIssuingBody())
                .issueDate(cert.getIssueDate())
                .expiryDate(cert.getExpiryDate())
                .credentialId(cert.getCredentialId())
                .build();

        certificationRepository.update(updated);

        List<Certification> afterUpdate = certificationRepository.findByRecordId(1L);
        assertThat(afterUpdate).anyMatch(c -> c.getName().equals("Updated Cert Name"));
    }

    @Test
    void deleteByRecordId_removesAllCertifications() {
        assertThat(certificationRepository.findByRecordId(1L)).isNotEmpty();

        certificationRepository.deleteByRecordId(1L);

        assertThat(certificationRepository.findByRecordId(1L)).isEmpty();
    }

    @Test
    void deleteByRecordIdExcluding_keepsSpecifiedIds() {
        List<Certification> existing = certificationRepository.findByRecordId(1L);
        assertThat(existing).hasSizeGreaterThanOrEqualTo(1);
        Long keepId = existing.get(0).getId();

        certificationRepository.deleteByRecordIdExcluding(1L, List.of(keepId));

        List<Certification> remaining = certificationRepository.findByRecordId(1L);
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getId()).isEqualTo(keepId);
    }

    @Test
    void deleteByRecordIdExcluding_emptyKeepList_deletesAll() {
        assertThat(certificationRepository.findByRecordId(1L)).isNotEmpty();

        certificationRepository.deleteByRecordIdExcluding(1L, List.of());

        assertThat(certificationRepository.findByRecordId(1L)).isEmpty();
    }
}
