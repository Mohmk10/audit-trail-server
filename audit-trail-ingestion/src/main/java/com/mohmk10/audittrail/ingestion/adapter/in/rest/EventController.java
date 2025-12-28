package com.mohmk10.audittrail.ingestion.adapter.in.rest;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.exception.EventNotFoundException;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.mapper.EventRequestMapper;
import com.mohmk10.audittrail.ingestion.service.EventIngestionService;
import com.mohmk10.audittrail.storage.service.ImmutableStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Validated
public class EventController {

    private final EventIngestionService eventIngestionService;
    private final ImmutableStorageService immutableStorageService;
    private final EventRequestMapper eventRequestMapper;

    public EventController(
            EventIngestionService eventIngestionService,
            ImmutableStorageService immutableStorageService,
            EventRequestMapper eventRequestMapper) {
        this.eventIngestionService = eventIngestionService;
        this.immutableStorageService = immutableStorageService;
        this.eventRequestMapper = eventRequestMapper;
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
