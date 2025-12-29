package com.mohmk10.audittrail.ingestion.service;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.core.exception.InvalidEventException;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ActionRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ActorRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventMetadataRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ResourceRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.mapper.EventRequestMapper;
import com.mohmk10.audittrail.search.service.EventIndexingService;
import com.mohmk10.audittrail.storage.service.ImmutableStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceImplTest {

    @Mock
    private EventValidationService validationService;

    @Mock
    private EventEnrichmentService enrichmentService;

    @Mock
    private ImmutableStorageService storageService;

    @Mock
    private EventRequestMapper mapper;

    @Mock
    private EventIndexingService indexingService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EventIngestionServiceImpl ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new EventIngestionServiceImpl(
                validationService,
                enrichmentService,
                storageService,
                mapper,
                indexingService,
                eventPublisher
        );
    }

    private EventRequest createValidEventRequest() {
        ActorRequest actor = new ActorRequest("actor-123", "USER", "John", null, null, null);
        ActionRequest action = new ActionRequest("CREATE", "Created document", null);
        ResourceRequest resource = new ResourceRequest("res-123", "DOCUMENT", "Report", null, null);
        EventMetadataRequest metadata = new EventMetadataRequest("web-app", "tenant-001", null, null, null, null);
        return new EventRequest(actor, action, resource, metadata);
    }

    private Event createTestEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, "Created", null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null,
                "hash-123",
                null
        );
    }

    @Test
    void shouldIngestValidEvent() {
        EventRequest request = createValidEventRequest();
        Event mappedEvent = createTestEvent();
        Event enrichedEvent = createTestEvent();
        Event storedEvent = createTestEvent();

        when(validationService.validate(request)).thenReturn(List.of());
        when(mapper.toEvent(request)).thenReturn(mappedEvent);
        when(enrichmentService.enrich(mappedEvent)).thenReturn(enrichedEvent);
        when(storageService.store(enrichedEvent)).thenReturn(storedEvent);

        Event result = ingestionService.ingest(request);

        assertThat(result).isEqualTo(storedEvent);
        verify(validationService).validate(request);
        verify(mapper).toEvent(request);
        verify(enrichmentService).enrich(mappedEvent);
        verify(storageService).store(enrichedEvent);
        verify(indexingService).index(storedEvent);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void shouldThrowExceptionForInvalidEvent() {
        EventRequest request = createValidEventRequest();
        List<String> violations = List.of("Invalid actor type", "Invalid action type");

        when(validationService.validate(request)).thenReturn(violations);

        assertThatThrownBy(() -> ingestionService.ingest(request))
                .isInstanceOf(InvalidEventException.class)
                .hasMessageContaining("Event validation failed");

        verify(mapper, never()).toEvent(any());
        verify(storageService, never()).store(any());
        verify(indexingService, never()).index(any());
    }

    @Test
    void shouldContinueOnIndexingFailure() {
        EventRequest request = createValidEventRequest();
        Event mappedEvent = createTestEvent();
        Event enrichedEvent = createTestEvent();
        Event storedEvent = createTestEvent();

        when(validationService.validate(request)).thenReturn(List.of());
        when(mapper.toEvent(request)).thenReturn(mappedEvent);
        when(enrichmentService.enrich(mappedEvent)).thenReturn(enrichedEvent);
        when(storageService.store(enrichedEvent)).thenReturn(storedEvent);
        doThrow(new RuntimeException("Indexing failed")).when(indexingService).index(storedEvent);

        Event result = ingestionService.ingest(request);

        assertThat(result).isEqualTo(storedEvent);
        verify(indexingService).index(storedEvent);
    }

    @Test
    void shouldIngestBatchSuccessfully() {
        EventRequest request1 = createValidEventRequest();
        EventRequest request2 = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(request1, request2));

        Event mappedEvent = createTestEvent();
        Event enrichedEvent = createTestEvent();
        Event storedEvent = createTestEvent();
        EventResponse response = new EventResponse(storedEvent.id(), storedEvent.timestamp(), storedEvent.hash(), "STORED");

        when(validationService.validate(any())).thenReturn(List.of());
        when(mapper.toEvent(any())).thenReturn(mappedEvent);
        when(enrichmentService.enrich(any())).thenReturn(enrichedEvent);
        when(storageService.store(any())).thenReturn(storedEvent);
        when(mapper.toResponse(any())).thenReturn(response);

        BatchEventResponse result = ingestionService.ingestBatch(batchRequest);

        assertThat(result.total()).isEqualTo(2);
        assertThat(result.succeeded()).isEqualTo(2);
        assertThat(result.failed()).isEqualTo(0);
        assertThat(result.events()).hasSize(2);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldHandlePartialBatchFailure() {
        EventRequest validRequest = createValidEventRequest();
        EventRequest invalidRequest = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(validRequest, invalidRequest, validRequest));

        Event mappedEvent = createTestEvent();
        Event enrichedEvent = createTestEvent();
        Event storedEvent = createTestEvent();
        EventResponse response = new EventResponse(storedEvent.id(), storedEvent.timestamp(), storedEvent.hash(), "STORED");

        // Use argument matchers more carefully - the validRequest object may be the same instance
        when(validationService.validate(any())).thenAnswer(invocation -> {
            EventRequest req = invocation.getArgument(0);
            // Second event (index 1) should fail
            return req == invalidRequest ? List.of("Invalid type") : List.of();
        });
        when(mapper.toEvent(any())).thenReturn(mappedEvent);
        when(enrichmentService.enrich(any())).thenReturn(enrichedEvent);
        when(storageService.store(any())).thenReturn(storedEvent);
        when(mapper.toResponse(any())).thenReturn(response);

        BatchEventResponse result = ingestionService.ingestBatch(batchRequest);

        assertThat(result.total()).isEqualTo(3);
        assertThat(result.succeeded()).isEqualTo(2);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.events()).hasSize(2);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0).index()).isEqualTo(1);
    }

    @Test
    void shouldHandleAllBatchFailures() {
        EventRequest invalidRequest = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(invalidRequest, invalidRequest));

        when(validationService.validate(any())).thenReturn(List.of("Invalid type"));

        BatchEventResponse result = ingestionService.ingestBatch(batchRequest);

        assertThat(result.total()).isEqualTo(2);
        assertThat(result.succeeded()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(2);
        assertThat(result.events()).isEmpty();
        assertThat(result.errors()).hasSize(2);
        verify(indexingService, never()).indexBatch(any());
    }

    @Test
    void shouldIndexBatchAfterSuccessfulStorage() {
        EventRequest request = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(request, request, request));

        Event mappedEvent = createTestEvent();
        Event enrichedEvent = createTestEvent();
        Event storedEvent = createTestEvent();
        EventResponse response = new EventResponse(storedEvent.id(), storedEvent.timestamp(), storedEvent.hash(), "STORED");

        when(validationService.validate(any())).thenReturn(List.of());
        when(mapper.toEvent(any())).thenReturn(mappedEvent);
        when(enrichmentService.enrich(any())).thenReturn(enrichedEvent);
        when(storageService.store(any())).thenReturn(storedEvent);
        when(mapper.toResponse(any())).thenReturn(response);

        ingestionService.ingestBatch(batchRequest);

        verify(indexingService).indexBatch(argThat(events -> events.size() == 3));
    }

    @Test
    void shouldContinueOnBatchIndexingFailure() {
        EventRequest request = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(request));

        Event mappedEvent = createTestEvent();
        Event enrichedEvent = createTestEvent();
        Event storedEvent = createTestEvent();
        EventResponse response = new EventResponse(storedEvent.id(), storedEvent.timestamp(), storedEvent.hash(), "STORED");

        when(validationService.validate(any())).thenReturn(List.of());
        when(mapper.toEvent(any())).thenReturn(mappedEvent);
        when(enrichmentService.enrich(any())).thenReturn(enrichedEvent);
        when(storageService.store(any())).thenReturn(storedEvent);
        when(mapper.toResponse(any())).thenReturn(response);
        doThrow(new RuntimeException("Batch indexing failed")).when(indexingService).indexBatch(any());

        BatchEventResponse result = ingestionService.ingestBatch(batchRequest);

        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(0);
    }

    @Test
    void shouldHandleStorageException() {
        EventRequest request = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(request));

        Event mappedEvent = createTestEvent();
        Event enrichedEvent = createTestEvent();

        when(validationService.validate(any())).thenReturn(List.of());
        when(mapper.toEvent(any())).thenReturn(mappedEvent);
        when(enrichmentService.enrich(any())).thenReturn(enrichedEvent);
        when(storageService.store(any())).thenThrow(new RuntimeException("Storage failed"));

        BatchEventResponse result = ingestionService.ingestBatch(batchRequest);

        assertThat(result.succeeded()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.errors().get(0).message()).contains("Storage failed");
    }

    @Test
    void shouldPublishEventForEachSuccessfulBatchItem() {
        EventRequest request = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(request, request));

        Event mappedEvent = createTestEvent();
        Event enrichedEvent = createTestEvent();
        Event storedEvent = createTestEvent();
        EventResponse response = new EventResponse(storedEvent.id(), storedEvent.timestamp(), storedEvent.hash(), "STORED");

        when(validationService.validate(any())).thenReturn(List.of());
        when(mapper.toEvent(any())).thenReturn(mappedEvent);
        when(enrichmentService.enrich(any())).thenReturn(enrichedEvent);
        when(storageService.store(any())).thenReturn(storedEvent);
        when(mapper.toResponse(any())).thenReturn(response);

        ingestionService.ingestBatch(batchRequest);

        verify(eventPublisher, times(2)).publishEvent(any());
    }

    @Test
    void shouldRecordCorrectErrorIndex() {
        // Create distinct request objects
        EventRequest validRequest1 = createValidEventRequest();
        EventRequest validRequest2 = createValidEventRequest();
        EventRequest invalidRequest1 = createValidEventRequest();
        EventRequest validRequest3 = createValidEventRequest();
        EventRequest invalidRequest2 = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(
                validRequest1, validRequest2, invalidRequest1, validRequest3, invalidRequest2
        ));

        Event mappedEvent = createTestEvent();
        Event enrichedEvent = createTestEvent();
        Event storedEvent = createTestEvent();
        EventResponse response = new EventResponse(storedEvent.id(), storedEvent.timestamp(), storedEvent.hash(), "STORED");

        // Track which requests should fail
        when(validationService.validate(any())).thenAnswer(invocation -> {
            EventRequest req = invocation.getArgument(0);
            return (req == invalidRequest1 || req == invalidRequest2) ? List.of("Invalid") : List.of();
        });
        when(mapper.toEvent(any())).thenReturn(mappedEvent);
        when(enrichmentService.enrich(any())).thenReturn(enrichedEvent);
        when(storageService.store(any())).thenReturn(storedEvent);
        when(mapper.toResponse(any())).thenReturn(response);

        BatchEventResponse result = ingestionService.ingestBatch(batchRequest);

        assertThat(result.errors()).hasSize(2);
        assertThat(result.errors().get(0).index()).isEqualTo(2);
        assertThat(result.errors().get(1).index()).isEqualTo(4);
    }
}
