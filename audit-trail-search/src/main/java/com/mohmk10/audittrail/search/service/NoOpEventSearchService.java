package com.mohmk10.audittrail.search.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;

@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEventSearchService implements EventSearchService {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventSearchService.class);

    public NoOpEventSearchService() {
        log.info("Elasticsearch is disabled. Search functionality will return empty results.");
    }

    @Override
    public SearchResult<Event> search(SearchCriteria criteria) {
        log.debug("Elasticsearch disabled - search returning empty results");
        return SearchResult.of(Collections.emptyList(), 0, criteria.page(), criteria.size());
    }

    @Override
    public SearchResult<Event> quickSearch(String query, String tenantId, Pageable pageable) {
        log.debug("Elasticsearch disabled - quickSearch returning empty results for query: {}", query);
        return SearchResult.of(Collections.emptyList(), 0, pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    public List<Event> findByCorrelationId(String correlationId) {
        log.debug("Elasticsearch disabled - findByCorrelationId returning empty results for: {}", correlationId);
        return Collections.emptyList();
    }

    @Override
    public List<Event> getTimeline(String tenantId, DateRange range, Pageable pageable) {
        log.debug("Elasticsearch disabled - getTimeline returning empty results for tenant: {}", tenantId);
        return Collections.emptyList();
    }

    @Override
    public Map<String, Long> aggregateByField(String tenantId, String field, DateRange range) {
        log.debug("Elasticsearch disabled - aggregateByField returning empty results for field: {}", field);
        return Collections.emptyMap();
    }
}
