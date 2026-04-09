package com.enterprise.shellapi.controller;

import com.enterprise.shellapi.dto.PagedResponse;
import com.enterprise.shellapi.dto.RecordRequest;
import com.enterprise.shellapi.model.Record;
import com.enterprise.shellapi.service.RecordService;
import jakarta.validation.Valid;
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
    public ResponseEntity<PagedResponse<Record>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String address) {
        return ResponseEntity.ok(recordService.search(name, email, department, status, address, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Record> findById(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Record> create(@Valid @RequestBody RecordRequest request) {
        Record created = recordService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Record> update(@PathVariable Long id, @Valid @RequestBody RecordRequest request) {
        return ResponseEntity.ok(recordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
