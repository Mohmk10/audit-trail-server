package com.mohmk10.audittrail.admin.adapter.in.rest;

import com.mohmk10.audittrail.admin.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.admin.domain.Source;
import com.mohmk10.audittrail.admin.service.SourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/sources")
public class AdminSourceController {

    private final SourceService sourceService;
    private final AdminDtoMapper dtoMapper;

    public AdminSourceController(SourceService sourceService, AdminDtoMapper dtoMapper) {
        this.sourceService = sourceService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping
    public ResponseEntity<SourceResponse> create(@Valid @RequestBody CreateSourceRequest request) {
        Source source = dtoMapper.toSourceDomain(request);
        Source created = sourceService.create(source);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toSourceResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<SourceResponse>> findByTenantId(@RequestParam String tenantId) {
        List<SourceResponse> sources = sourceService.findByTenantId(tenantId).stream()
                .map(dtoMapper::toSourceResponse)
                .toList();
        return ResponseEntity.ok(sources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceResponse> findById(@PathVariable UUID id) {
        return sourceService.findById(id)
                .map(dtoMapper::toSourceResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SourceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSourceRequest request) {
        Source source = dtoMapper.toSourceDomain(request);
        Source updated = sourceService.update(id, source);
        return ResponseEntity.ok(dtoMapper.toSourceResponse(updated));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        sourceService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        sourceService.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
