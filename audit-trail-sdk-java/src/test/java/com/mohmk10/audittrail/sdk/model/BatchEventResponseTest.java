package com.mohmk10.audittrail.sdk.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BatchEventResponseTest {

    @Test
    void shouldCreateWithDefaultConstructor() {
        BatchEventResponse response = new BatchEventResponse();

        assertThat(response.getTotal()).isZero();
        assertThat(response.getSucceeded()).isZero();
        assertThat(response.getFailed()).isZero();
        assertThat(response.getEvents()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldSetAndGetTotal() {
        BatchEventResponse response = new BatchEventResponse();
        response.setTotal(100);

        assertThat(response.getTotal()).isEqualTo(100);
    }

    @Test
    void shouldSetAndGetSucceeded() {
        BatchEventResponse response = new BatchEventResponse();
        response.setSucceeded(95);

        assertThat(response.getSucceeded()).isEqualTo(95);
    }

    @Test
    void shouldSetAndGetFailed() {
        BatchEventResponse response = new BatchEventResponse();
        response.setFailed(5);

        assertThat(response.getFailed()).isEqualTo(5);
    }

    @Test
    void shouldSetAndGetEvents() {
        BatchEventResponse response = new BatchEventResponse();
        List<EventResponse> events = Arrays.asList(
                new EventResponse(UUID.randomUUID(), Instant.now(), "hash1", "STORED"),
                new EventResponse(UUID.randomUUID(), Instant.now(), "hash2", "STORED")
        );

        response.setEvents(events);

        assertThat(response.getEvents()).hasSize(2);
    }

    @Test
    void shouldSetAndGetErrors() {
        BatchEventResponse response = new BatchEventResponse();
        BatchEventResponse.ErrorDetail error1 = createErrorDetail(0, "Validation failed", Arrays.asList("Actor ID required"));
        BatchEventResponse.ErrorDetail error2 = createErrorDetail(2, "Invalid action", Arrays.asList("Action type required"));

        response.setErrors(Arrays.asList(error1, error2));

        assertThat(response.getErrors()).hasSize(2);
    }

    @Test
    void shouldTrackSuccessfulBatch() {
        BatchEventResponse response = new BatchEventResponse();
        response.setTotal(10);
        response.setSucceeded(10);
        response.setFailed(0);

        assertThat(response.getTotal()).isEqualTo(10);
        assertThat(response.getSucceeded()).isEqualTo(10);
        assertThat(response.getFailed()).isZero();
    }

    @Test
    void shouldTrackPartialFailure() {
        BatchEventResponse response = new BatchEventResponse();
        response.setTotal(10);
        response.setSucceeded(8);
        response.setFailed(2);

        assertThat(response.getTotal()).isEqualTo(10);
        assertThat(response.getSucceeded()).isEqualTo(8);
        assertThat(response.getFailed()).isEqualTo(2);
    }

    @Test
    void shouldTrackCompleteFailure() {
        BatchEventResponse response = new BatchEventResponse();
        response.setTotal(5);
        response.setSucceeded(0);
        response.setFailed(5);

        assertThat(response.getTotal()).isEqualTo(5);
        assertThat(response.getSucceeded()).isZero();
        assertThat(response.getFailed()).isEqualTo(5);
    }

    @Test
    void shouldHandleEmptyBatch() {
        BatchEventResponse response = new BatchEventResponse();
        response.setTotal(0);
        response.setSucceeded(0);
        response.setFailed(0);
        response.setEvents(List.of());
        response.setErrors(List.of());

        assertThat(response.getTotal()).isZero();
        assertThat(response.getEvents()).isEmpty();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldCreateErrorDetailWithIndex() {
        BatchEventResponse.ErrorDetail error = new BatchEventResponse.ErrorDetail();
        error.setIndex(5);

        assertThat(error.getIndex()).isEqualTo(5);
    }

    @Test
    void shouldCreateErrorDetailWithMessage() {
        BatchEventResponse.ErrorDetail error = new BatchEventResponse.ErrorDetail();
        error.setMessage("Validation error");

        assertThat(error.getMessage()).isEqualTo("Validation error");
    }

    @Test
    void shouldCreateErrorDetailWithViolations() {
        BatchEventResponse.ErrorDetail error = new BatchEventResponse.ErrorDetail();
        List<String> violations = Arrays.asList(
                "Actor ID is required",
                "Action type must not be empty"
        );
        error.setViolations(violations);

        assertThat(error.getViolations()).hasSize(2);
        assertThat(error.getViolations()).contains("Actor ID is required");
    }

    @Test
    void shouldMapErrorsToEventIndices() {
        BatchEventResponse response = new BatchEventResponse();
        response.setTotal(5);
        response.setSucceeded(3);
        response.setFailed(2);

        BatchEventResponse.ErrorDetail error1 = createErrorDetail(1, "Invalid actor", List.of("Actor ID required"));
        BatchEventResponse.ErrorDetail error2 = createErrorDetail(3, "Invalid resource", List.of("Resource type required"));

        response.setErrors(Arrays.asList(error1, error2));

        assertThat(response.getErrors().get(0).getIndex()).isEqualTo(1);
        assertThat(response.getErrors().get(1).getIndex()).isEqualTo(3);
    }

    @Test
    void shouldHandleLargeBatch() {
        BatchEventResponse response = new BatchEventResponse();
        response.setTotal(1000);
        response.setSucceeded(998);
        response.setFailed(2);

        assertThat(response.getTotal()).isEqualTo(1000);
        assertThat(response.getSucceeded()).isEqualTo(998);
    }

    private BatchEventResponse.ErrorDetail createErrorDetail(int index, String message, List<String> violations) {
        BatchEventResponse.ErrorDetail error = new BatchEventResponse.ErrorDetail();
        error.setIndex(index);
        error.setMessage(message);
        error.setViolations(violations);
        return error;
    }
}
