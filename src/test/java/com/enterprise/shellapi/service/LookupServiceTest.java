package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.LookupsResponse;
import com.enterprise.shellapi.repository.LookupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LookupServiceTest {

    @Mock
    private LookupRepository lookupRepository;

    @InjectMocks
    private LookupService lookupService;

    @Test
    void getAllLookups_returnsAllCategories() {
        when(lookupRepository.findDepartments()).thenReturn(List.of("Engineering", "Marketing"));
        when(lookupRepository.findStatuses()).thenReturn(List.of("active", "inactive"));
        when(lookupRepository.findEmploymentTypes()).thenReturn(List.of("full-time"));
        when(lookupRepository.findNotificationChannels()).thenReturn(List.of("email", "slack"));
        when(lookupRepository.findAccessLevels()).thenReturn(List.of("standard", "admin"));
        when(lookupRepository.findRelationships()).thenReturn(List.of("Spouse"));
        when(lookupRepository.findPayFrequencies()).thenReturn(List.of("annual", "monthly"));

        LookupsResponse result = lookupService.getAllLookups();

        assertThat(result.getDepartments()).hasSize(2);
        assertThat(result.getStatuses()).hasSize(2);
        assertThat(result.getEmploymentTypes()).hasSize(1);
        assertThat(result.getNotificationChannels()).hasSize(2);
        assertThat(result.getAccessLevels()).hasSize(2);
        assertThat(result.getRelationships()).hasSize(1);
        assertThat(result.getPayFrequencies()).hasSize(2);
        assertThat(result.getPayFrequencies().get(0).getValue()).isEqualTo("annual");
        assertThat(result.getPayFrequencies().get(0).getLabel()).isEqualTo("Annual");
    }
}
