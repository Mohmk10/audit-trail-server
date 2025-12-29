package com.mohmk10.audittrail.ingestion.adapter.in.rest.exception;

import com.mohmk10.audittrail.core.exception.EventNotFoundException;
import com.mohmk10.audittrail.core.exception.InvalidEventException;
import com.mohmk10.audittrail.core.exception.StorageException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/events");
    }

    @Test
    void shouldHandleInvalidEventException() {
        List<String> violations = List.of("Invalid actor type", "Invalid action type");
        InvalidEventException exception = new InvalidEventException("Validation failed", violations);

        ResponseEntity<ApiError> response = exceptionHandler.handleInvalidEventException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/events");
        assertThat(response.getBody().details()).containsExactlyElementsOf(violations);
    }

    @Test
    void shouldHandleEventNotFoundException() {
        UUID eventId = UUID.randomUUID();
        EventNotFoundException exception = new EventNotFoundException(eventId);

        ResponseEntity<ApiError> response = exceptionHandler.handleEventNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).contains(eventId.toString());
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void shouldHandleConstraintViolationException() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("Actor ID is required");
        when(violation2.getMessage()).thenReturn("Action type is required");

        Set<ConstraintViolation<?>> violations = Set.of(violation1, violation2);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<ApiError> response = exceptionHandler.handleConstraintViolationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Error");
        assertThat(response.getBody().message()).isEqualTo("Constraint violation");
        assertThat(response.getBody().details()).hasSize(2);
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("eventRequest", "actor.id", "Actor ID is required");
        FieldError fieldError2 = new FieldError("eventRequest", "action.type", "Action type is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = exceptionHandler.handleMethodArgumentNotValidException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Error");
        assertThat(response.getBody().message()).isEqualTo("Request validation failed");
        assertThat(response.getBody().details()).hasSize(2);
        assertThat(response.getBody().details()).anyMatch(d -> d.contains("actor.id"));
        assertThat(response.getBody().details()).anyMatch(d -> d.contains("action.type"));
    }

    @Test
    void shouldHandleStorageException() {
        StorageException exception = new StorageException("Database connection failed");

        ResponseEntity<ApiError> response = exceptionHandler.handleStorageException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Storage Error");
        assertThat(response.getBody().message()).isEqualTo("Database connection failed");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void shouldHandleGenericException() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<ApiError> response = exceptionHandler.handleGenericException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().details()).contains("Unexpected error");
    }

    @Test
    void shouldIncludeTimestampInResponse() {
        InvalidEventException exception = new InvalidEventException("Error", List.of());

        ResponseEntity<ApiError> response = exceptionHandler.handleInvalidEventException(exception, request);

        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void shouldIncludePathInResponse() {
        when(request.getRequestURI()).thenReturn("/api/v1/events/batch");
        InvalidEventException exception = new InvalidEventException("Error", List.of());

        ResponseEntity<ApiError> response = exceptionHandler.handleInvalidEventException(exception, request);

        assertThat(response.getBody().path()).isEqualTo("/api/v1/events/batch");
    }

    @Test
    void shouldHandleEmptyViolationsList() {
        InvalidEventException exception = new InvalidEventException("Validation failed", List.of());

        ResponseEntity<ApiError> response = exceptionHandler.handleInvalidEventException(exception, request);

        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void shouldHandleNullPointerException() {
        Exception exception = new NullPointerException("Null value encountered");

        ResponseEntity<ApiError> response = exceptionHandler.handleGenericException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().details()).contains("Null value encountered");
    }

    @Test
    void shouldPreserveExceptionMessage() {
        String detailedMessage = "Event with ID 123 could not be stored due to hash chain violation";
        StorageException exception = new StorageException(detailedMessage);

        ResponseEntity<ApiError> response = exceptionHandler.handleStorageException(exception, request);

        assertThat(response.getBody().message()).isEqualTo(detailedMessage);
    }
}
