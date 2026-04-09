package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.LookupOption;
import com.enterprise.shellapi.dto.LookupsResponse;
import com.enterprise.shellapi.repository.LookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LookupService {

    private final LookupRepository lookupRepository;

    public LookupsResponse getAllLookups() {
        return LookupsResponse.builder()
                .departments(toOptions(lookupRepository.findDepartments()))
                .relationships(toOptions(lookupRepository.findRelationships()))
                .statuses(toLabeledOptions(lookupRepository.findStatuses()))
                .employmentTypes(toLabeledOptions(lookupRepository.findEmploymentTypes()))
                .notificationChannels(toLabeledOptions(lookupRepository.findNotificationChannels()))
                .accessLevels(toLabeledOptions(lookupRepository.findAccessLevels()))
                .build();
    }

    private List<LookupOption> toOptions(List<String> values) {
        return values.stream()
                .map(v -> LookupOption.builder().value(v).label(v).build())
                .toList();
    }

    private List<LookupOption> toLabeledOptions(List<String> values) {
        return values.stream()
                .map(v -> LookupOption.builder().value(v).label(toLabel(v)).build())
                .toList();
    }

    private String toLabel(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return java.util.Arrays.stream(value.split("-"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
