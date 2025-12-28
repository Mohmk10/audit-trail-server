package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.TenantEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.mapper.TenantMapper;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.JpaTenantRepository;
import com.mohmk10.audittrail.admin.domain.Tenant;
import com.mohmk10.audittrail.admin.domain.TenantPlan;
import com.mohmk10.audittrail.admin.domain.TenantQuota;
import com.mohmk10.audittrail.admin.domain.TenantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TenantServiceImpl implements TenantService {

    private final JpaTenantRepository repository;
    private final TenantMapper mapper;

    public TenantServiceImpl(JpaTenantRepository repository, TenantMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Tenant create(Tenant tenant) {
        if (tenant.getId() == null) {
            tenant.setId(UUID.randomUUID());
        }
        if (tenant.getStatus() == null) {
            tenant.setStatus(TenantStatus.ACTIVE);
        }
        if (tenant.getPlan() == null) {
            tenant.setPlan(TenantPlan.FREE);
        }
        if (tenant.getQuota() == null) {
            tenant.setQuota(getDefaultQuota(tenant.getPlan()));
        }
        if (tenant.getSlug() == null || tenant.getSlug().isBlank()) {
            tenant.setSlug(generateSlug(tenant.getName()));
        }
        tenant.setCreatedAt(Instant.now());
        tenant.setUpdatedAt(Instant.now());

        TenantEntity entity = mapper.toEntity(tenant);
        TenantEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Tenant update(UUID id, Tenant tenant) {
        TenantEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        if (tenant.getName() != null) {
            existing.setName(tenant.getName());
        }
        if (tenant.getDescription() != null) {
            existing.setDescription(tenant.getDescription());
        }
        if (tenant.getPlan() != null) {
            existing.setPlan(tenant.getPlan());
        }
        if (tenant.getQuota() != null) {
            existing.setQuota(mapper.toEntity(Tenant.builder().quota(tenant.getQuota()).build()).getQuota());
        }
        if (tenant.getSettings() != null) {
            existing.setSettings(tenant.getSettings());
        }
        existing.setUpdatedAt(Instant.now());

        TenantEntity saved = repository.save(existing);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findBySlug(String slug) {
        return repository.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void suspend(UUID id) {
        TenantEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        entity.setStatus(TenantStatus.SUSPENDED);
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);
    }

    @Override
    public void activate(UUID id) {
        TenantEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        entity.setStatus(TenantStatus.ACTIVE);
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);
    }

    @Override
    public void delete(UUID id) {
        TenantEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        entity.setStatus(TenantStatus.DELETED);
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);
    }

    @Override
    public TenantQuota getDefaultQuota(TenantPlan plan) {
        return switch (plan) {
            case FREE -> new TenantQuota(1000, 30000, 2, 2, 1, 7);
            case STARTER -> new TenantQuota(10000, 300000, 5, 5, 5, 30);
            case PRO -> new TenantQuota(100000, 3000000, 20, 20, 20, 90);
            case ENTERPRISE -> new TenantQuota(Long.MAX_VALUE, Long.MAX_VALUE, 100, 100, 100, 365);
        };
    }

    private String generateSlug(String name) {
        if (name == null) {
            return UUID.randomUUID().toString().substring(0, 8);
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
