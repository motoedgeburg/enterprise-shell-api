package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.EmergencyContactRequest;
import com.enterprise.shellapi.dto.HistoryRequest;
import com.enterprise.shellapi.dto.PersonalInfoRequest;
import com.enterprise.shellapi.dto.PreferencesRequest;
import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.dto.WorkInfoRequest;
import com.enterprise.shellapi.exception.ValidationException;
import com.enterprise.shellapi.repository.LookupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordRequestValidatorTest {

    @Mock
    private LookupRepository lookupRepository;

    @InjectMocks
    private RecordRequestValidator validator;

    private RecordRequest validRequest() {
        return RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("Test").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Engineer").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .build();
    }

    private void stubAllLookups() {
        when(lookupRepository.findDepartments()).thenReturn(List.of("Engineering", "Product", "Design"));
        when(lookupRepository.findStatuses()).thenReturn(List.of("active", "inactive"));
        when(lookupRepository.findEmploymentTypes()).thenReturn(List.of("full-time", "part-time", "contract", "intern"));
    }

    @Test
    void validRequest_passes() {
        stubAllLookups();
        assertThatCode(() -> validator.validate(validRequest())).doesNotThrowAnyException();
    }

    @Test
    void invalidDepartment_throwsWithField() {
        when(lookupRepository.findDepartments()).thenReturn(List.of("Engineering", "Product"));
        when(lookupRepository.findStatuses()).thenReturn(List.of("active", "inactive"));
        when(lookupRepository.findEmploymentTypes()).thenReturn(List.of("full-time"));

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("T").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("InvalidDept")
                        .status("active").employmentType("full-time").build())
                .build();

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getFieldErrors()).hasSize(1);
                    assertThat(ve.getFieldErrors().get(0).getField()).isEqualTo("workInfo.department");
                });
    }

    @Test
    void invalidStatus_throwsWithField() {
        when(lookupRepository.findDepartments()).thenReturn(List.of("Engineering"));
        when(lookupRepository.findStatuses()).thenReturn(List.of("active", "inactive"));
        when(lookupRepository.findEmploymentTypes()).thenReturn(List.of("full-time"));

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("T").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("suspended").employmentType("full-time").build())
                .build();

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getFieldErrors().get(0).getField()).isEqualTo("workInfo.status");
                });
    }

    @Test
    void invalidEmploymentType_throwsWithField() {
        when(lookupRepository.findDepartments()).thenReturn(List.of("Engineering"));
        when(lookupRepository.findStatuses()).thenReturn(List.of("active"));
        when(lookupRepository.findEmploymentTypes()).thenReturn(List.of("full-time", "part-time"));

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("T").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("freelance").build())
                .build();

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getFieldErrors().get(0).getField()).isEqualTo("workInfo.employmentType");
                });
    }

    @Test
    void invalidAccessLevel_throwsWithField() {
        stubAllLookups();
        when(lookupRepository.findAccessLevels()).thenReturn(List.of("read-only", "standard", "admin"));

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("T").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .preferences(PreferencesRequest.builder().accessLevel("superadmin").build())
                .build();

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getFieldErrors().get(0).getField()).isEqualTo("preferences.accessLevel");
                });
    }

    @Test
    void invalidRelationship_throwsWithField() {
        stubAllLookups();
        when(lookupRepository.findRelationships()).thenReturn(List.of("Spouse", "Parent", "Sibling"));

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("T").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("Engineering")
                        .status("active").employmentType("full-time").build())
                .history(HistoryRequest.builder()
                        .emergencyContacts(List.of(EmergencyContactRequest.builder()
                                .name("Contact").relationship("Neighbor").build()))
                        .build())
                .build();

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getFieldErrors().get(0).getField()).isEqualTo("history.emergencyContacts[0].relationship");
                });
    }

    @Test
    void multipleErrors_reportsAll() {
        when(lookupRepository.findDepartments()).thenReturn(List.of("Engineering"));
        when(lookupRepository.findStatuses()).thenReturn(List.of("active"));
        when(lookupRepository.findEmploymentTypes()).thenReturn(List.of("full-time"));

        RecordRequest request = RecordRequest.builder()
                .personalInfo(PersonalInfoRequest.builder().name("T").email("t@t.com").build())
                .workInfo(WorkInfoRequest.builder()
                        .jobTitle("Eng").department("BadDept")
                        .status("bad").employmentType("bad").build())
                .build();

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException ve = (ValidationException) ex;
                    assertThat(ve.getFieldErrors()).hasSize(3);
                });
    }
}
