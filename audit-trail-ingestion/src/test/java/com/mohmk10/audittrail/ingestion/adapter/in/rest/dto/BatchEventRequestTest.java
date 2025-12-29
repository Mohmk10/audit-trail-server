package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BatchEventRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private EventRequest validEvent() {
        ActorRequest actor = new ActorRequest("actor-123", "USER", "John", null, null, null);
        ActionRequest action = new ActionRequest("CREATE", "Created", null);
        ResourceRequest resource = new ResourceRequest("res-123", "DOCUMENT", "Report", null, null);
        return new EventRequest(actor, action, resource, null);
    }

    @Test
    void shouldBeValidWithSingleEvent() {
        List<EventRequest> events = List.of(validEvent());
        BatchEventRequest request = new BatchEventRequest(events);

        Set<ConstraintViolation<BatchEventRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.events()).hasSize(1);
    }

    @Test
    void shouldBeValidWithMultipleEvents() {
        List<EventRequest> events = List.of(validEvent(), validEvent(), validEvent());
        BatchEventRequest request = new BatchEventRequest(events);

        Set<ConstraintViolation<BatchEventRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.events()).hasSize(3);
    }

    @Test
    void shouldFailValidationWhenEventsListIsNull() {
        BatchEventRequest request = new BatchEventRequest(null);

        Set<ConstraintViolation<BatchEventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Events list cannot be empty");
    }

    @Test
    void shouldFailValidationWhenEventsListIsEmpty() {
        BatchEventRequest request = new BatchEventRequest(List.of());

        Set<ConstraintViolation<BatchEventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Events list cannot be empty");
    }

    @Test
    void shouldFailValidationWhenExceedsMaxBatchSize() {
        List<EventRequest> events = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            events.add(validEvent());
        }
        BatchEventRequest request = new BatchEventRequest(events);

        Set<ConstraintViolation<BatchEventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Maximum 1000 events per batch");
    }

    @Test
    void shouldBeValidWithMaxBatchSize() {
        List<EventRequest> events = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            events.add(validEvent());
        }
        BatchEventRequest request = new BatchEventRequest(events);

        Set<ConstraintViolation<BatchEventRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.events()).hasSize(1000);
    }

    @Test
    void shouldCascadeValidationToNestedEvents() {
        EventRequest invalidEvent = new EventRequest(null, null, null, null);
        List<EventRequest> events = List.of(validEvent(), invalidEvent);
        BatchEventRequest request = new BatchEventRequest(events);

        Set<ConstraintViolation<BatchEventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldValidateAllEventsInBatch() {
        ActorRequest invalidActor = new ActorRequest(null, "USER", null, null, null, null);
        EventRequest invalidEvent1 = new EventRequest(invalidActor,
                new ActionRequest("CREATE", null, null),
                new ResourceRequest("res-1", "DOCUMENT", null, null, null), null);
        EventRequest invalidEvent2 = new EventRequest(invalidActor,
                new ActionRequest("UPDATE", null, null),
                new ResourceRequest("res-2", "DOCUMENT", null, null, null), null);

        List<EventRequest> events = List.of(invalidEvent1, invalidEvent2);
        BatchEventRequest request = new BatchEventRequest(events);

        Set<ConstraintViolation<BatchEventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    @Test
    void shouldPreserveEventsOrder() {
        ActorRequest actor1 = new ActorRequest("actor-1", "USER", null, null, null, null);
        ActorRequest actor2 = new ActorRequest("actor-2", "SYSTEM", null, null, null, null);
        ActorRequest actor3 = new ActorRequest("actor-3", "SERVICE", null, null, null, null);

        EventRequest event1 = new EventRequest(actor1, new ActionRequest("CREATE", null, null),
                new ResourceRequest("res-1", "DOCUMENT", null, null, null), null);
        EventRequest event2 = new EventRequest(actor2, new ActionRequest("UPDATE", null, null),
                new ResourceRequest("res-2", "DOCUMENT", null, null, null), null);
        EventRequest event3 = new EventRequest(actor3, new ActionRequest("DELETE", null, null),
                new ResourceRequest("res-3", "DOCUMENT", null, null, null), null);

        List<EventRequest> events = List.of(event1, event2, event3);
        BatchEventRequest request = new BatchEventRequest(events);

        assertThat(request.events().get(0).actor().id()).isEqualTo("actor-1");
        assertThat(request.events().get(1).actor().id()).isEqualTo("actor-2");
        assertThat(request.events().get(2).actor().id()).isEqualTo("actor-3");
    }

    @Test
    void shouldHandleMixedValidAndInvalidEvents() {
        EventRequest validEvent = validEvent();
        EventRequest invalidEvent = new EventRequest(null, null, null, null);

        List<EventRequest> events = List.of(validEvent, invalidEvent, validEvent);
        BatchEventRequest request = new BatchEventRequest(events);

        Set<ConstraintViolation<BatchEventRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}
