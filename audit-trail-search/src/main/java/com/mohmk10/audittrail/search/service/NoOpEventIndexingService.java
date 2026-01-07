package com.mohmk10.audittrail.search.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.mohmk10.audittrail.core.domain.Event;

@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEventIndexingService implements EventIndexingService {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventIndexingService.class);

    public NoOpEventIndexingService() {
        log.info("Elasticsearch is disabled. Event indexing will be skipped.");
    }

    @Override
    public void index(Event event) {
        log.debug("Elasticsearch disabled - skipping index for event: {}",
            event != null ? event.id() : "null");
    }

    @Override
    public void indexBatch(List<Event> events) {
        log.debug("Elasticsearch disabled - skipping batch index for {} events",
            events != null ? events.size() : 0);
    }

    @Override
    public void reindexAll() {
        log.warn("Elasticsearch disabled - reindexAll() has no effect");
    }

    @Override
    public void deleteIndex() {
        log.warn("Elasticsearch disabled - deleteIndex() has no effect");
    }

    @Override
    public void deleteByTenantId(String tenantId) {
        log.warn("Elasticsearch disabled - deleteByTenantId() has no effect for tenant: {}", tenantId);
    }
}
