package com.mohmk10.audittrail.search.adapter.out;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.AggregationRequest;
import com.mohmk10.audittrail.core.dto.AggregationResult;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.core.port.out.EventSearchPort;
import com.mohmk10.audittrail.search.service.EventSearchService;

@Component
public class ElasticsearchEventSearchAdapter implements EventSearchPort {

    private final EventSearchService eventSearchService;

    public ElasticsearchEventSearchAdapter(EventSearchService eventSearchService) {
        this.eventSearchService = eventSearchService;
    }

    @Override
    public SearchResult<Event> search(SearchCriteria criteria) {
        return eventSearchService.search(criteria);
    }

    @Override
    public AggregationResult aggregate(AggregationRequest request) {
        Map<String, Long> counts = eventSearchService.aggregateByField(
                request.baseCriteria().tenantId(),
                request.groupByField(),
                request.baseCriteria().dateRange()
        );

        var buckets = new ArrayList<AggregationResult.Bucket>();
        counts.forEach((key, value) -> buckets.add(
                new AggregationResult.Bucket(key, value, Collections.emptyMap())));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        return new AggregationResult(buckets, total);
    }
}
