package com.mohmk10.audittrail.admin.adapter.in.rest;

import com.mohmk10.audittrail.admin.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.admin.domain.ApiKey;
import com.mohmk10.audittrail.admin.domain.ApiKeyCreationResult;
import com.mohmk10.audittrail.admin.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final AdminDtoMapper dtoMapper;

    public ApiKeyController(ApiKeyService apiKeyService, AdminDtoMapper dtoMapper) {
        this.apiKeyService = apiKeyService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping
    public ResponseEntity<ApiKeyCreatedResponse> create(@Valid @RequestBody CreateApiKeyRequest request) {
        ApiKey apiKey = dtoMapper.toApiKeyDomain(request);
        ApiKeyCreationResult result = apiKeyService.create(apiKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toApiKeyCreatedResponse(result));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> findByTenantId(@RequestParam String tenantId) {
        List<ApiKeyResponse> apiKeys = apiKeyService.findByTenantId(tenantId).stream()
                .map(dtoMapper::toApiKeyResponse)
                .toList();
        return ResponseEntity.ok(apiKeys);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiKeyResponse> findById(@PathVariable UUID id) {
        return apiKeyService.findById(id)
                .map(dtoMapper::toApiKeyResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/source/{sourceId}")
    public ResponseEntity<List<ApiKeyResponse>> findBySourceId(@PathVariable UUID sourceId) {
        List<ApiKeyResponse> apiKeys = apiKeyService.findBySourceId(sourceId).stream()
                .map(dtoMapper::toApiKeyResponse)
                .toList();
        return ResponseEntity.ok(apiKeys);
    }

    @PostMapping("/{id}/rotate")
    public ResponseEntity<RotateApiKeyResponse> rotate(@PathVariable UUID id) {
        ApiKeyCreationResult result = apiKeyService.rotate(id);
        ApiKey newKey = result.apiKey();
        return ResponseEntity.ok(RotateApiKeyResponse.from(
                id,
                newKey.getId(),
                newKey.getTenantId(),
                newKey.getSourceId(),
                newKey.getName(),
                result.plainTextKey(),
                newKey.getKeyPrefix(),
                newKey.getScopes(),
                newKey.getStatus(),
                newKey.getCreatedAt(),
                newKey.getExpiresAt()
        ));
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        apiKeyService.revoke(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        apiKeyService.revoke(id);
        return ResponseEntity.noContent().build();
    }
}
