package com.mohmk10.audittrail.ingestion.adapter.in.rest;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.exception.EventNotFoundException;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.mapper.EventRequestMapper;
import com.mohmk10.audittrail.ingestion.service.EventIngestionService;
import com.mohmk10.audittrail.search.service.EventSearchService;
import com.mohmk10.audittrail.storage.service.ImmutableStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Validated
public class EventController {

    private final EventIngestionService eventIngestionService;
    private final ImmutableStorageService immutableStorageService;
    private final EventSearchService eventSearchService;
    private final EventRequestMapper eventRequestMapper;

    public EventController(
            EventIngestionService eventIngestionService,
            ImmutableStorageService immutableStorageService,
            EventSearchService eventSearchService,
            EventRequestMapper eventRequestMapper) {
        this.eventIngestionService = eventIngestionService;
        this.immutableStorageService = immutableStorageService;
        this.eventSearchService = eventSearchService;
        this.eventRequestMapper = eventRequestMapper;
    }

    @GetMapping
    public ResponseEntity<SearchResult<Event>> getEvents(
            @RequestParam(required = false) String tenantId,
            @RequestHeader(value = "X-Tenant-ID", required = false) String headerTenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String query
    ) {
        String effectiveTenantId = tenantId != null ? tenantId : headerTenantId;
        if (effectiveTenantId == null || effectiveTenantId.isBlank()) {
            effectiveTenantId = "production";
        }

        DateRange dateRange = null;
        if (from != null || to != null) {
            dateRange = new DateRange(from, to);
        }

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId(effectiveTenantId)
                .actorId(actorId)
                .dateRange(dateRange)
                .query(query)
                .page(page)
                .size(size)
                .build();

        SearchResult<Event> result = eventSearchService.search(criteria);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<EventResponse> ingestEvent(@RequestBody @Valid EventRequest request) {
        Event storedEvent = eventIngestionService.ingest(request);
        EventResponse response = eventRequestMapper.toResponse(storedEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchEventResponse> ingestBatch(@RequestBody @Valid BatchEventRequest request) {
        BatchEventResponse response = eventIngestionService.ingestBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable UUID id) {
        Event event = immutableStorageService.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
        EventResponse response = eventRequestMapper.toResponse(event);
        return ResponseEntity.ok(response);
    }
}
