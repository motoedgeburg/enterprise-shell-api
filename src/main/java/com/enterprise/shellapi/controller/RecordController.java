package com.enterprise.shellapi.controller;

import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.dto.RecordSummary;
import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.service.RecordService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping
    public ResponseEntity<List<RecordSummary>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String address) {
        return ResponseEntity.ok(recordService.search(name, email, department, status, address));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Record> findByUuid(@PathVariable String uuid) {
        return ResponseEntity.ok(recordService.findByUuid(uuid));
    }

    @PostMapping
    public ResponseEntity<Record> create(@Valid @RequestBody RecordRequest request) {
        Record created = recordService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<Record> update(@PathVariable String uuid, @Valid @RequestBody RecordRequest request) {
        return ResponseEntity.ok(recordService.update(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid) {
        recordService.delete(uuid);
        return ResponseEntity.noContent().build();
    }
}
