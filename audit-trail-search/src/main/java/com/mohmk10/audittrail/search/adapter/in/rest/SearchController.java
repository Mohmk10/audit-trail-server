package com.mohmk10.audittrail.search.adapter.in.rest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.core.dto.AggregationRequest;
import com.mohmk10.audittrail.core.dto.AggregationResult;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.search.adapter.in.rest.dto.AggregationRequestDto;
import com.mohmk10.audittrail.search.adapter.in.rest.dto.EventSearchResponse;
import com.mohmk10.audittrail.search.adapter.in.rest.dto.SearchRequest;
import com.mohmk10.audittrail.search.service.EventSearchService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final EventSearchService eventSearchService;

    public SearchController(EventSearchService eventSearchService) {
        this.eventSearchService = eventSearchService;
    }

    @PostMapping
    public ResponseEntity<SearchResult<EventSearchResponse>> search(@RequestBody @Valid SearchRequest request) {
        SearchCriteria criteria = buildSearchCriteria(request);
        SearchResult<Event> result = eventSearchService.search(criteria);
        return ResponseEntity.ok(mapToResponse(result));
    }

    @GetMapping("/quick")
    public ResponseEntity<SearchResult<EventSearchResponse>> quickSearch(
            @RequestParam String q,
            @RequestParam String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "timestamp"));
        SearchResult<Event> result = eventSearchService.quickSearch(q, tenantId, pageable);
        return ResponseEntity.ok(mapToResponse(result));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<EventSearchResponse>> timeline(
            @RequestParam String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        DateRange range = new DateRange(from, to);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        List<Event> events = eventSearchService.getTimeline(tenantId, range, pageable);
        return ResponseEntity.ok(events.stream().map(EventSearchResponse::from).toList());
    }

    @PostMapping("/aggregations")
    public ResponseEntity<AggregationResult> aggregate(@RequestBody @Valid AggregationRequestDto request) {
        DateRange dateRange = null;
        if (request.fromDate() != null || request.toDate() != null) {
            dateRange = new DateRange(request.fromDate(), request.toDate());
        }

        Map<String, Long> counts = eventSearchService.aggregateByField(
                request.tenantId(),
                request.groupByField(),
                dateRange
        );

        var buckets = new ArrayList<AggregationResult.Bucket>();
        counts.forEach((key, value) -> buckets.add(
                new AggregationResult.Bucket(key, value, Collections.emptyMap())));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        AggregationResult result = new AggregationResult(buckets, total);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<EventSearchResponse>> findByCorrelation(@PathVariable String correlationId) {
        List<Event> events = eventSearchService.findByCorrelationId(correlationId);
        return ResponseEntity.ok(events.stream().map(EventSearchResponse::from).toList());
    }

    private SearchCriteria buildSearchCriteria(SearchRequest request) {
        DateRange dateRange = null;
        if (request.fromDate() != null || request.toDate() != null) {
            dateRange = new DateRange(request.fromDate(), request.toDate());
        }

        List<Action.ActionType> actionTypes = null;
        if (request.actionType() != null && !request.actionType().isBlank()) {
            try {
                actionTypes = List.of(Action.ActionType.valueOf(request.actionType()));
            } catch (IllegalArgumentException ignored) {}
        }

        List<Resource.ResourceType> resourceTypes = null;
        if (request.resourceType() != null && !request.resourceType().isBlank()) {
            try {
                resourceTypes = List.of(Resource.ResourceType.valueOf(request.resourceType()));
            } catch (IllegalArgumentException ignored) {}
        }

        Map<String, String> tags = null;
        if (request.tags() != null && !request.tags().isEmpty()) {
            tags = new java.util.HashMap<>();
            for (String tag : request.tags()) {
                String[] parts = tag.split(":", 2);
                if (parts.length == 2) {
                    tags.put(parts[0], parts[1]);
                }
            }
        }

        SearchCriteria.SortDirection sortDirection = SearchCriteria.SortDirection.DESC;
        if ("asc".equalsIgnoreCase(request.sortOrder())) {
            sortDirection = SearchCriteria.SortDirection.ASC;
        }

        return SearchCriteria.builder()
                .tenantId(request.tenantId())
                .actorId(request.actorId())
                .actionTypes(actionTypes)
                .resourceTypes(resourceTypes)
                .dateRange(dateRange)
                .query(request.query())
                .tags(tags)
                .page(request.page())
                .size(request.size())
                .sortBy(request.sortBy())
                .sortDirection(sortDirection)
                .build();
    }

    private SearchResult<EventSearchResponse> mapToResponse(SearchResult<Event> result) {
        List<EventSearchResponse> responses = result.items().stream()
                .map(EventSearchResponse::from)
                .toList();

        return SearchResult.of(responses, result.totalCount(), result.page(), result.size());
    }
}
