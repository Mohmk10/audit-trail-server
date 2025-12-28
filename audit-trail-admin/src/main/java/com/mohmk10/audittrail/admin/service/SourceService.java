package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.domain.Source;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SourceService {
    Source create(Source source);
    Source update(UUID id, Source source);
    Optional<Source> findById(UUID id);
    List<Source> findByTenantId(String tenantId);
    void activate(UUID id);
    void deactivate(UUID id);
    void delete(UUID id);
    void incrementEventCount(UUID id);
    void updateLastEventAt(UUID id);
}
