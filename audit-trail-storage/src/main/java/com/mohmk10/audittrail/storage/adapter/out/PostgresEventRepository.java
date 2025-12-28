package com.mohmk10.audittrail.storage.adapter.out;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.port.out.EventRepository;
import com.mohmk10.audittrail.storage.adapter.out.persistence.mapper.EventMapper;
import com.mohmk10.audittrail.storage.adapter.out.persistence.repository.JpaEventRepository;
import com.mohmk10.audittrail.storage.service.ImmutableStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresEventRepository implements EventRepository {

    private final ImmutableStorageService immutableStorageService;
    private final JpaEventRepository jpaEventRepository;

    public PostgresEventRepository(
            ImmutableStorageService immutableStorageService,
            JpaEventRepository jpaEventRepository) {
        this.immutableStorageService = immutableStorageService;
        this.jpaEventRepository = jpaEventRepository;
    }

    @Override
    @Transactional
    public Event save(Event event) {
        return immutableStorageService.store(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(UUID id) {
        return immutableStorageService.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> findByTenantId(String tenantId, Pageable pageable) {
        return jpaEventRepository.findByTenantIdOrderByTimestampDesc(tenantId, pageable)
                .map(EventMapper::toDomain);
    }
}
