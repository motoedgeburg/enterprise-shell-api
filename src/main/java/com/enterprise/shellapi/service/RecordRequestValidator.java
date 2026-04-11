package com.enterprise.shellapi.service;

import com.enterprise.shellapi.dto.EmergencyContactRequest;
import com.enterprise.shellapi.dto.ErrorResponse;
import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.exception.ValidationException;
import com.enterprise.shellapi.repository.LookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecordRequestValidator {

    private final LookupRepository lookupRepository;

    public void validate(RecordRequest request) {
        List<ErrorResponse.FieldError> errors = new ArrayList<>();

        if (request.getWorkInfo() != null) {
            validateLookup(errors, "workInfo.department",
                    request.getWorkInfo().getDepartment(), lookupRepository.findDepartments());
            validateLookup(errors, "workInfo.status",
                    request.getWorkInfo().getStatus(), lookupRepository.findStatuses());
            validateLookup(errors, "workInfo.employmentType",
                    request.getWorkInfo().getEmploymentType(), lookupRepository.findEmploymentTypes());
        }

        if (request.getPreferences() != null && request.getPreferences().getAccessLevel() != null) {
            validateLookup(errors, "preferences.accessLevel",
                    request.getPreferences().getAccessLevel(), lookupRepository.findAccessLevels());
        }

        if (request.getHistory() != null && request.getHistory().getEmergencyContacts() != null) {
            List<String> validRelationships = lookupRepository.findRelationships();
            List<EmergencyContactRequest> contacts = request.getHistory().getEmergencyContacts();
            for (int i = 0; i < contacts.size(); i++) {
                EmergencyContactRequest ec = contacts.get(i);
                if (ec.getRelationship() != null) {
                    validateLookup(errors, "history.emergencyContacts[" + i + "].relationship",
                            ec.getRelationship(), validRelationships);
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void validateLookup(List<ErrorResponse.FieldError> errors, String field,
                                 String value, List<String> validValues) {
        if (value == null) return;
        Set<String> allowed = Set.copyOf(validValues);
        if (!allowed.contains(value)) {
            String options = validValues.stream().collect(Collectors.joining(", "));
            errors.add(ErrorResponse.FieldError.builder()
                    .field(field)
                    .message("Must be one of: " + options)
                    .build());
        }
    }
}
