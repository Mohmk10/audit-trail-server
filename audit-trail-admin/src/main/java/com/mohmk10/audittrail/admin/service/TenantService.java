package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.domain.Tenant;
import com.mohmk10.audittrail.admin.domain.TenantPlan;
import com.mohmk10.audittrail.admin.domain.TenantQuota;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantService {
    Tenant create(Tenant tenant);
    Tenant update(UUID id, Tenant tenant);
    Optional<Tenant> findById(UUID id);
    Optional<Tenant> findBySlug(String slug);
    List<Tenant> findAll();
    void suspend(UUID id);
    void activate(UUID id);
    void delete(UUID id);
    TenantQuota getDefaultQuota(TenantPlan plan);
}
