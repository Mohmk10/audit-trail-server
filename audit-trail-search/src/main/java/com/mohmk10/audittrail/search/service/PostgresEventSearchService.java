package com.mohmk10.audittrail.search.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import com.mohmk10.audittrail.storage.adapter.out.persistence.mapper.EventMapper;
import com.mohmk10.audittrail.storage.adapter.out.persistence.repository.JpaEventRepository;

@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "false", matchIfMissing = true)
public class PostgresEventSearchService implements EventSearchService {

    private static final Logger log = LoggerFactory.getLogger(PostgresEventSearchService.class);

    private final JpaEventRepository eventRepository;

    public PostgresEventSearchService(JpaEventRepository eventRepository) {
        this.eventRepository = eventRepository;
        log.info("PostgreSQL-based search service initialized (Elasticsearch disabled)");
    }

    @Override
    public SearchResult<Event> search(SearchCriteria criteria) {
        Pageable pageable = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        Page<EventEntity> page;

        // Handle different search scenarios with available JPA methods
        if (criteria.query() != null && !criteria.query().isBlank()) {
            // Full-text search using LIKE
            page = eventRepository.quickSearch(criteria.tenantId(), criteria.query(), pageable);
        } else if (criteria.dateRange() != null) {
            // Date range filter
            Instant from = criteria.dateRange().from();
            Instant to = criteria.dateRange().to() != null ? criteria.dateRange().to() : Instant.now();
            page = eventRepository.findByTenantIdAndTimestampBetween(criteria.tenantId(), from, to, pageable);
        } else if (criteria.actorId() != null && !criteria.actorId().isBlank()) {
            // Actor filter
            if (criteria.actionTypes() != null && !criteria.actionTypes().isEmpty()) {
                // Actor + ActionType filter
                String actionType = criteria.actionTypes().get(0).name();
                page = eventRepository.findByTenantIdAndActorIdAndActionTypeOrderByTimestampDesc(
                        criteria.tenantId(), criteria.actorId(), actionType, pageable);
            } else {
                page = eventRepository.findByTenantIdAndActorIdOrderByTimestampDesc(
                        criteria.tenantId(), criteria.actorId(), pageable);
            }
        } else if (criteria.actionTypes() != null && !criteria.actionTypes().isEmpty()) {
            // ActionType filter
            String actionType = criteria.actionTypes().get(0).name();
            page = eventRepository.findByTenantIdAndActionTypeOrderByTimestampDesc(
                    criteria.tenantId(), actionType, pageable);
        } else if (criteria.resourceTypes() != null && !criteria.resourceTypes().isEmpty()) {
            // ResourceType filter
            String resourceType = criteria.resourceTypes().get(0).name();
            page = eventRepository.findByTenantIdAndResourceTypeOrderByTimestampDesc(
                    criteria.tenantId(), resourceType, pageable);
        } else {
            // Default: just paginated by tenant
            page = eventRepository.findByTenantIdOrderByTimestampDesc(criteria.tenantId(), pageable);
        }

        List<Event> events = page.getContent().stream()
                .map(EventMapper::toDomain)
                .toList();

        return SearchResult.of(events, page.getTotalElements(), criteria.page(), criteria.size());
    }

    @Override
    public SearchResult<Event> quickSearch(String query, String tenantId, Pageable pageable) {
        if (query == null || query.isBlank()) {
            Page<EventEntity> page = eventRepository.findByTenantIdOrderByTimestampDesc(tenantId, pageable);
            List<Event> events = page.getContent().stream()
                    .map(EventMapper::toDomain)
                    .toList();
            return SearchResult.of(events, page.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize());
        }

        Page<EventEntity> page = eventRepository.quickSearch(tenantId, query, pageable);
        List<Event> events = page.getContent().stream()
                .map(EventMapper::toDomain)
                .toList();

        return SearchResult.of(events, page.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    public List<Event> findByCorrelationId(String correlationId) {
        return eventRepository.findByCorrelationId(correlationId).stream()
                .map(EventMapper::toDomain)
                .toList();
    }

    @Override
    public List<Event> getTimeline(String tenantId, DateRange range, Pageable pageable) {
        Instant from = range != null && range.from() != null ? range.from() : Instant.EPOCH;
        Instant to = range != null && range.to() != null ? range.to() : Instant.now();

        Page<EventEntity> page = eventRepository.findByTenantIdAndTimestampBetween(tenantId, from, to, pageable);
        return page.getContent().stream()
                .map(EventMapper::toDomain)
                .toList();
    }

    @Override
    public Map<String, Long> aggregateByField(String tenantId, String field, DateRange range) {
        Instant from = range != null ? range.from() : null;
        Instant to = range != null ? range.to() : null;

        List<Object[]> results;
        switch (field.toLowerCase()) {
            case "actiontype":
            case "action_type":
                results = eventRepository.countByActionType(tenantId, from, to);
                break;
            case "resourcetype":
            case "resource_type":
                results = eventRepository.countByResourceType(tenantId, from, to);
                break;
            case "actorid":
            case "actor_id":
                results = eventRepository.countByActorId(tenantId, from, to);
                break;
            default:
                log.warn("Unsupported aggregation field: {}. Returning empty results.", field);
                return new HashMap<>();
        }

        Map<String, Long> aggregations = new HashMap<>();
        for (Object[] row : results) {
            String key = row[0] != null ? row[0].toString() : "unknown";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            aggregations.put(key, count);
        }
        return aggregations;
    }
}
