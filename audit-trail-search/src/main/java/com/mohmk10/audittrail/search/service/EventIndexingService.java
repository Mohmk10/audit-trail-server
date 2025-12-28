package com.mohmk10.audittrail.search.service;

import java.util.List;

import com.mohmk10.audittrail.core.domain.Event;

public interface EventIndexingService {

    void index(Event event);

    void indexBatch(List<Event> events);

    void reindexAll();

    void deleteIndex();

    void deleteByTenantId(String tenantId);
}
