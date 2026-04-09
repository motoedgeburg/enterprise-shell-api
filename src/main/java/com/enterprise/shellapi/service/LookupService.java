package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.LookupsResponse;
import com.enterprise.shellapi.repository.LookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LookupService {

    private final LookupRepository lookupRepository;

    public LookupsResponse getAllLookups() {
        return LookupsResponse.builder()
                .departments(lookupRepository.findDepartments())
                .relationships(lookupRepository.findRelationships())
                .statuses(lookupRepository.findStatuses())
                .employmentTypes(lookupRepository.findEmploymentTypes())
                .notificationChannels(lookupRepository.findNotificationChannels())
                .accessLevels(lookupRepository.findAccessLevels())
                .build();
    }
}
