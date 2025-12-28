package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.SourceEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.mapper.SourceMapper;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.JpaSourceRepository;
import com.mohmk10.audittrail.admin.domain.Source;
import com.mohmk10.audittrail.admin.domain.SourceStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SourceServiceImpl implements SourceService {

    private final JpaSourceRepository repository;
    private final SourceMapper mapper;

    public SourceServiceImpl(JpaSourceRepository repository, SourceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Source create(Source source) {
        if (source.getId() == null) {
            source.setId(UUID.randomUUID());
        }
        if (source.getStatus() == null) {
            source.setStatus(SourceStatus.ACTIVE);
        }
        source.setCreatedAt(Instant.now());
        source.setEventCount(0);

        SourceEntity entity = mapper.toEntity(source);
        SourceEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Source update(UUID id, Source source) {
        SourceEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + id));

        if (source.getName() != null) {
            existing.setName(source.getName());
        }
        if (source.getDescription() != null) {
            existing.setDescription(source.getDescription());
        }
        if (source.getType() != null) {
            existing.setType(source.getType());
        }
        if (source.getConfig() != null) {
            existing.setConfig(source.getConfig());
        }

        SourceEntity saved = repository.save(existing);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Source> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Source> findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void activate(UUID id) {
        SourceEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + id));
        entity.setStatus(SourceStatus.ACTIVE);
        repository.save(entity);
    }

    @Override
    public void deactivate(UUID id) {
        SourceEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + id));
        entity.setStatus(SourceStatus.INACTIVE);
        repository.save(entity);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void incrementEventCount(UUID id) {
        repository.incrementEventCount(id);
    }

    @Override
    public void updateLastEventAt(UUID id) {
        repository.updateLastEventAt(id, Instant.now());
    }
}
