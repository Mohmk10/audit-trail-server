package com.mohmk10.audittrail.ingestion.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.exception.InvalidEventException;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ErrorDetail;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.mapper.EventRequestMapper;
import com.mohmk10.audittrail.storage.service.ImmutableStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventIngestionServiceImpl implements EventIngestionService {

    private final EventValidationService validationService;
    private final EventEnrichmentService enrichmentService;
    private final ImmutableStorageService storageService;
    private final EventRequestMapper mapper;

    public EventIngestionServiceImpl(
            EventValidationService validationService,
            EventEnrichmentService enrichmentService,
            ImmutableStorageService storageService,
            EventRequestMapper mapper) {
        this.validationService = validationService;
        this.enrichmentService = enrichmentService;
        this.storageService = storageService;
        this.mapper = mapper;
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
        return storageService.store(enrichedEvent);
    }

    @Override
    @Transactional
    public BatchEventResponse ingestBatch(BatchEventRequest request) {
        List<EventResponse> successfulEvents = new ArrayList<>();
        List<ErrorDetail> errors = new ArrayList<>();

        for (int i = 0; i < request.events().size(); i++) {
            EventRequest eventRequest = request.events().get(i);
            try {
                Event storedEvent = ingestSingleEvent(eventRequest);
                successfulEvents.add(mapper.toResponse(storedEvent));
            } catch (InvalidEventException e) {
                errors.add(new ErrorDetail(i, e.getMessage(), e.getViolations()));
            } catch (Exception e) {
                errors.add(new ErrorDetail(i, e.getMessage(), List.of()));
            }
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
}
