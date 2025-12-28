package com.mohmk10.audittrail.admin.adapter.in.rest;

import com.mohmk10.audittrail.admin.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.admin.domain.Tenant;
import com.mohmk10.audittrail.admin.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final AdminDtoMapper dtoMapper;

    public TenantController(TenantService tenantService, AdminDtoMapper dtoMapper) {
        this.tenantService = tenantService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = dtoMapper.toTenantDomain(request);
        Tenant created = tenantService.create(tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toTenantResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> findAll() {
        List<TenantResponse> tenants = tenantService.findAll().stream()
                .map(dtoMapper::toTenantResponse)
                .toList();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> findById(@PathVariable UUID id) {
        return tenantService.findById(id)
                .map(dtoMapper::toTenantResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<TenantResponse> findBySlug(@PathVariable String slug) {
        return tenantService.findBySlug(slug)
                .map(dtoMapper::toTenantResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request) {
        Tenant tenant = dtoMapper.toTenantDomain(request);
        Tenant updated = tenantService.update(id, tenant);
        return ResponseEntity.ok(dtoMapper.toTenantResponse(updated));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable UUID id) {
        tenantService.suspend(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        tenantService.activate(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
