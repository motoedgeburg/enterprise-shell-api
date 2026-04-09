package com.enterprise.shellapi.controller;

import com.enterprise.shellapi.dto.LookupsResponse;
import com.enterprise.shellapi.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lookups")
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;

    @GetMapping
    public ResponseEntity<LookupsResponse> getLookups() {
        return ResponseEntity.ok(lookupService.getAllLookups());
    }
}
