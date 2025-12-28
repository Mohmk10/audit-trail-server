package com.mohmk10.audittrail.core.port.out;

import com.mohmk10.audittrail.core.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {

    Event save(Event event);

    Optional<Event> findById(UUID id);

    Page<Event> findByTenantId(String tenantId, Pageable pageable);
}
