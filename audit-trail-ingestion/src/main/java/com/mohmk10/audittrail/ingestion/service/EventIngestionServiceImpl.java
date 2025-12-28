package com.mohmk10.audittrail.ingestion.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.exception.InvalidEventException;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ErrorDetail;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.mapper.EventRequestMapper;
import com.mohmk10.audittrail.search.service.EventIndexingService;
import com.mohmk10.audittrail.storage.service.ImmutableStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventIngestionServiceImpl implements EventIngestionService {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionServiceImpl.class);

    private final EventValidationService validationService;
    private final EventEnrichmentService enrichmentService;
    private final ImmutableStorageService storageService;
    private final EventRequestMapper mapper;
    private final EventIndexingService indexingService;

    public EventIngestionServiceImpl(
            EventValidationService validationService,
            EventEnrichmentService enrichmentService,
            ImmutableStorageService storageService,
            EventRequestMapper mapper,
            EventIndexingService indexingService) {
        this.validationService = validationService;
        this.enrichmentService = enrichmentService;
        this.storageService = storageService;
        this.mapper = mapper;
        this.indexingService = indexingService;
    }

    @Override
    @Transactional
    public Event ingest(EventRequest request) {
        List<String> violations = validationService.validate(request);
        if (!violations.isEmpty()) {
            throw new InvalidEventException("Event validation failed", violations);
        }

        Event event = mapper.toEvent(request);
        Event enrichedEvent = enrichmentService.enrich(event);
        Event storedEvent = storageService.store(enrichedEvent);

        indexEvent(storedEvent);

        return storedEvent;
    }

    @Override
    @Transactional
    public BatchEventResponse ingestBatch(BatchEventRequest request) {
        List<EventResponse> successfulEvents = new ArrayList<>();
        List<ErrorDetail> errors = new ArrayList<>();
        List<Event> eventsToIndex = new ArrayList<>();

        for (int i = 0; i < request.events().size(); i++) {
            EventRequest eventRequest = request.events().get(i);
            try {
                Event storedEvent = ingestSingleEvent(eventRequest);
                successfulEvents.add(mapper.toResponse(storedEvent));
                eventsToIndex.add(storedEvent);
            } catch (InvalidEventException e) {
                errors.add(new ErrorDetail(i, e.getMessage(), e.getViolations()));
            } catch (Exception e) {
                errors.add(new ErrorDetail(i, e.getMessage(), List.of()));
            }
        }

        if (!eventsToIndex.isEmpty()) {
            indexEvents(eventsToIndex);
        }

        return new BatchEventResponse(
                request.events().size(),
                successfulEvents.size(),
                errors.size(),
                successfulEvents,
                errors
        );
    }

    private Event ingestSingleEvent(EventRequest request) {
        List<String> violations = validationService.validate(request);
        if (!violations.isEmpty()) {
            throw new InvalidEventException("Event validation failed", violations);
        }

        Event event = mapper.toEvent(request);
        Event enrichedEvent = enrichmentService.enrich(event);
        return storageService.store(enrichedEvent);
    }

    private void indexEvent(Event event) {
        try {
            indexingService.index(event);
        } catch (Exception e) {
            log.error("Failed to index event {}: {}", event.id(), e.getMessage());
        }
    }

    private void indexEvents(List<Event> events) {
        try {
            indexingService.indexBatch(events);
        } catch (Exception e) {
            log.error("Failed to index {} events: {}", events.size(), e.getMessage());
        }
    }
}
