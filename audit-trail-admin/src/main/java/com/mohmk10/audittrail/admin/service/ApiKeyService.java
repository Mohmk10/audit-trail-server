package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.domain.ApiKey;
import com.mohmk10.audittrail.admin.domain.ApiKeyCreationResult;
import com.mohmk10.audittrail.admin.domain.ApiKeyScope;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreationResult create(ApiKey apiKey);
    Optional<ApiKey> findById(UUID id);
    Optional<ApiKey> findByKey(String key);
    List<ApiKey> findByTenantId(String tenantId);
    List<ApiKey> findBySourceId(UUID sourceId);
    ApiKeyCreationResult rotate(UUID id);
    void revoke(UUID id);
    boolean validate(String key, ApiKeyScope requiredScope);
    void updateLastUsed(UUID id, String ip);
}
