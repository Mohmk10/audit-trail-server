package com.mohmk10.audittrail.ingestion.adapter.in.rest;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.core.exception.EventNotFoundException;
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
import com.mohmk10.audittrail.ingestion.service.EventIngestionService;
import com.mohmk10.audittrail.storage.service.ImmutableStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventIngestionService eventIngestionService;

    @Mock
    private ImmutableStorageService immutableStorageService;

    @Mock
    private EventRequestMapper eventRequestMapper;

    private EventController controller;

    @BeforeEach
    void setUp() {
        controller = new EventController(eventIngestionService, immutableStorageService, eventRequestMapper);
    }

    private EventRequest createValidEventRequest() {
        ActorRequest actor = new ActorRequest("actor-123", "USER", "John Doe", null, null, null);
        ActionRequest action = new ActionRequest("CREATE", "Created document", null);
        ResourceRequest resource = new ResourceRequest("res-123", "DOCUMENT", "Annual Report", null, null);
        EventMetadataRequest metadata = new EventMetadataRequest("web-app", "tenant-001", null, null, null, null);
        return new EventRequest(actor, action, resource, metadata);
    }

    private Event createTestEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John Doe", null, null, null),
                new Action(Action.ActionType.CREATE, "Created", null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null,
                "hash-123",
                null
        );
    }

    @Test
    void shouldIngestEventSuccessfully() {
        EventRequest request = createValidEventRequest();
        Event storedEvent = createTestEvent();
        EventResponse response = new EventResponse(storedEvent.id(), storedEvent.timestamp(), storedEvent.hash(), "STORED");

        when(eventIngestionService.ingest(any())).thenReturn(storedEvent);
        when(eventRequestMapper.toResponse(storedEvent)).thenReturn(response);

        ResponseEntity<EventResponse> result = controller.ingestEvent(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().hash()).isEqualTo("hash-123");
        assertThat(result.getBody().status()).isEqualTo("STORED");
    }

    @Test
    void shouldThrowExceptionForInvalidEvent() {
        EventRequest request = createValidEventRequest();

        when(eventIngestionService.ingest(any()))
                .thenThrow(new InvalidEventException("Validation failed", List.of("Invalid actor type")));

        assertThatThrownBy(() -> controller.ingestEvent(request))
                .isInstanceOf(InvalidEventException.class)
                .hasMessageContaining("Validation failed");
    }

    @Test
    void shouldIngestBatchSuccessfully() {
        EventRequest request = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(request, request));
        Event storedEvent = createTestEvent();
        EventResponse response = new EventResponse(storedEvent.id(), storedEvent.timestamp(), storedEvent.hash(), "STORED");
        BatchEventResponse batchResponse = new BatchEventResponse(2, 2, 0, List.of(response, response), List.of());

        when(eventIngestionService.ingestBatch(any())).thenReturn(batchResponse);

        ResponseEntity<BatchEventResponse> result = controller.ingestBatch(batchRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().total()).isEqualTo(2);
        assertThat(result.getBody().succeeded()).isEqualTo(2);
        assertThat(result.getBody().failed()).isEqualTo(0);
    }

    @Test
    void shouldGetEventById() {
        UUID eventId = UUID.randomUUID();
        Event event = createTestEvent();
        EventResponse response = new EventResponse(event.id(), event.timestamp(), event.hash(), "STORED");

        when(immutableStorageService.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRequestMapper.toResponse(event)).thenReturn(response);

        ResponseEntity<EventResponse> result = controller.getEvent(eventId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().hash()).isEqualTo("hash-123");
    }

    @Test
    void shouldThrowExceptionForNonExistentEvent() {
        UUID eventId = UUID.randomUUID();

        when(immutableStorageService.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getEvent(eventId))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void shouldReturnCreatedStatus() {
        EventRequest request = createValidEventRequest();
        Event storedEvent = createTestEvent();
        EventResponse response = new EventResponse(storedEvent.id(), storedEvent.timestamp(), storedEvent.hash(), "STORED");

        when(eventIngestionService.ingest(any())).thenReturn(storedEvent);
        when(eventRequestMapper.toResponse(storedEvent)).thenReturn(response);

        ResponseEntity<EventResponse> result = controller.ingestEvent(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void shouldReturnCreatedStatusForBatch() {
        EventRequest request = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(request));
        BatchEventResponse batchResponse = new BatchEventResponse(1, 1, 0, List.of(), List.of());

        when(eventIngestionService.ingestBatch(any())).thenReturn(batchResponse);

        ResponseEntity<BatchEventResponse> result = controller.ingestBatch(batchRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void shouldReturnOkStatusForGetEvent() {
        UUID eventId = UUID.randomUUID();
        Event event = createTestEvent();
        EventResponse response = new EventResponse(event.id(), event.timestamp(), event.hash(), "STORED");

        when(immutableStorageService.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRequestMapper.toResponse(event)).thenReturn(response);

        ResponseEntity<EventResponse> result = controller.getEvent(eventId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldHandleBatchWithPartialFailures() {
        EventRequest request = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest(List.of(request, request, request));
        BatchEventResponse batchResponse = new BatchEventResponse(3, 2, 1, List.of(), List.of());

        when(eventIngestionService.ingestBatch(any())).thenReturn(batchResponse);

        ResponseEntity<BatchEventResponse> result = controller.ingestBatch(batchRequest);

        assertThat(result.getBody().succeeded()).isEqualTo(2);
        assertThat(result.getBody().failed()).isEqualTo(1);
    }
}
