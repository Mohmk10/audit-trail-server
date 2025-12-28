package com.mohmk10.audittrail.search.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;

public interface EventSearchService {

    SearchResult<Event> search(SearchCriteria criteria);

    SearchResult<Event> quickSearch(String query, String tenantId, Pageable pageable);

    List<Event> findByCorrelationId(String correlationId);

    List<Event> getTimeline(String tenantId, DateRange range, Pageable pageable);

    Map<String, Long> aggregateByField(String tenantId, String field, DateRange range);
}
