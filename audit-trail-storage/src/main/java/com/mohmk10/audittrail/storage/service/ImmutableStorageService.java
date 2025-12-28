package com.mohmk10.audittrail.storage.service;

import com.mohmk10.audittrail.core.domain.Event;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImmutableStorageService {

    Event store(Event event);

    List<Event> storeBatch(List<Event> events);

    Optional<Event> findById(UUID id);

    boolean verifyIntegrity(UUID eventId);
}
