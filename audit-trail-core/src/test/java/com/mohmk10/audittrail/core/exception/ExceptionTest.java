package com.mohmk10.audittrail.core.exception;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionTest {

    @Test
    void shouldCreateInvalidEventException() {
        InvalidEventException exception = new InvalidEventException("Invalid event data", null);

        assertThat(exception.getMessage()).isEqualTo("Invalid event data");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldCreateInvalidEventExceptionWithViolations() {
        List<String> violations = List.of(
                "Actor ID is required",
                "Timestamp must not be null",
                "Resource name is blank"
        );

        InvalidEventException exception = new InvalidEventException("Validation failed", violations);

        assertThat(exception.getMessage()).isEqualTo("Validation failed");
        assertThat(exception.getViolations()).hasSize(3);
        assertThat(exception.getViolations()).contains("Actor ID is required");
        assertThat(exception.getViolations()).contains("Timestamp must not be null");
        assertThat(exception.getViolations()).contains("Resource name is blank");
    }

    @Test
    void shouldHandleEmptyViolationsList() {
        InvalidEventException exception = new InvalidEventException("No violations", List.of());

        assertThat(exception.getViolations()).isEmpty();
    }

    @Test
    void shouldHandleNullViolationsList() {
        InvalidEventException exception = new InvalidEventException("No violations", null);

        assertThat(exception.getViolations()).isNull();
    }

    @Test
    void shouldCreateEventNotFoundException() {
        UUID eventId = UUID.randomUUID();

        EventNotFoundException exception = new EventNotFoundException(eventId);

        assertThat(exception.getMessage()).contains("Event not found");
        assertThat(exception.getMessage()).contains(eventId.toString());
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldCreateEventNotFoundExceptionWithId() {
        UUID eventId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        EventNotFoundException exception = new EventNotFoundException(eventId);

        assertThat(exception.getEventId()).isEqualTo(eventId);
        assertThat(exception.getMessage()).isEqualTo("Event not found: 550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void shouldPreserveEventIdInNotFoundException() {
        UUID originalId = UUID.randomUUID();

        EventNotFoundException exception = new EventNotFoundException(originalId);

        assertThat(exception.getEventId()).isEqualTo(originalId);
    }

    @Test
    void shouldCreateStorageException() {
        StorageException exception = new StorageException("Database connection failed");

        assertThat(exception.getMessage()).isEqualTo("Database connection failed");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldCreateStorageExceptionWithCause() {
        Exception cause = new RuntimeException("Connection refused");

        StorageException exception = new StorageException("Failed to store event", cause);

        assertThat(exception.getMessage()).isEqualTo("Failed to store event");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Connection refused");
    }

    @Test
    void shouldThrowInvalidEventException() {
        assertThatThrownBy(() -> {
            throw new InvalidEventException("Test exception", List.of("violation1"));
        })
                .isInstanceOf(InvalidEventException.class)
                .hasMessage("Test exception");
    }

    @Test
    void shouldThrowEventNotFoundException() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> {
            throw new EventNotFoundException(id);
        })
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void shouldThrowStorageException() {
        assertThatThrownBy(() -> {
            throw new StorageException("Storage error", new RuntimeException("Disk full"));
        })
                .isInstanceOf(StorageException.class)
                .hasMessage("Storage error")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldBeRuntimeExceptions() {
        assertThat(new InvalidEventException("msg", null)).isInstanceOf(RuntimeException.class);
        assertThat(new EventNotFoundException(UUID.randomUUID())).isInstanceOf(RuntimeException.class);
        assertThat(new StorageException("msg")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldHandleStorageExceptionWithNullCause() {
        StorageException exception = new StorageException("Error message");

        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo("Error message");
    }

    @Test
    void shouldChainExceptionCauses() {
        Exception rootCause = new IllegalStateException("Root cause");
        Exception intermediateCause = new RuntimeException("Intermediate", rootCause);
        StorageException exception = new StorageException("Top level error", intermediateCause);

        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }
}
