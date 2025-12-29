package com.mohmk10.audittrail.sdk.exception;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTrailValidationExceptionTest {

    @Test
    void shouldCreateExceptionWithMessageOnly() {
        AuditTrailValidationException exception = new AuditTrailValidationException("Validation failed");

        assertThat(exception.getMessage()).isEqualTo("Validation failed");
        assertThat(exception.getViolations()).isEmpty();
    }

    @Test
    void shouldCreateExceptionWithMessageAndViolations() {
        List<String> violations = Arrays.asList(
                "Field 'email' is required",
                "Field 'name' must not be empty"
        );

        AuditTrailValidationException exception = new AuditTrailValidationException("Validation failed", violations);

        assertThat(exception.getMessage()).isEqualTo("Validation failed");
        assertThat(exception.getViolations()).hasSize(2);
        assertThat(exception.getViolations()).contains("Field 'email' is required");
        assertThat(exception.getViolations()).contains("Field 'name' must not be empty");
    }

    @Test
    void shouldExtendAuditTrailException() {
        AuditTrailValidationException exception = new AuditTrailValidationException("Test");

        assertThat(exception).isInstanceOf(AuditTrailException.class);
    }

    @Test
    void shouldReturnUnmodifiableViolationsList() {
        List<String> violations = Arrays.asList("Error 1", "Error 2");
        AuditTrailValidationException exception = new AuditTrailValidationException("Validation failed", violations);

        List<String> returnedViolations = exception.getViolations();

        assertThat(returnedViolations).isUnmodifiable();
    }

    @Test
    void shouldHandleEmptyViolationsList() {
        AuditTrailValidationException exception = new AuditTrailValidationException("Validation failed", Collections.emptyList());

        assertThat(exception.getViolations()).isEmpty();
    }

    @Test
    void shouldHandleNullViolationsList() {
        AuditTrailValidationException exception = new AuditTrailValidationException("Validation failed", null);

        assertThat(exception.getViolations()).isEmpty();
    }

    @Test
    void shouldPreserveViolationOrder() {
        List<String> violations = Arrays.asList("First error", "Second error", "Third error");
        AuditTrailValidationException exception = new AuditTrailValidationException("Validation failed", violations);

        assertThat(exception.getViolations().get(0)).isEqualTo("First error");
        assertThat(exception.getViolations().get(1)).isEqualTo("Second error");
        assertThat(exception.getViolations().get(2)).isEqualTo("Third error");
    }

    @Test
    void shouldHandleSingleViolation() {
        List<String> violations = Collections.singletonList("Single error");
        AuditTrailValidationException exception = new AuditTrailValidationException("Validation failed", violations);

        assertThat(exception.getViolations()).hasSize(1);
        assertThat(exception.getViolations().get(0)).isEqualTo("Single error");
    }

    @Test
    void shouldHandleMultipleViolations() {
        List<String> violations = Arrays.asList(
                "Email is invalid",
                "Password must be at least 8 characters",
                "Username is already taken",
                "Phone number format is invalid"
        );

        AuditTrailValidationException exception = new AuditTrailValidationException("Multiple validation errors", violations);

        assertThat(exception.getViolations()).hasSize(4);
    }

    @Test
    void shouldWrapViolationsAsUnmodifiable() {
        List<String> violations = Arrays.asList("Error 1", "Error 2");
        AuditTrailValidationException exception = new AuditTrailValidationException("Validation failed", violations);

        // Violations list should be unmodifiable
        assertThat(exception.getViolations()).isUnmodifiable();
        assertThat(exception.getViolations()).hasSize(2);
    }

    @Test
    void shouldSupportActorValidation() {
        List<String> violations = Arrays.asList(
                "Actor ID is required",
                "Actor type must be one of: user, system, service"
        );

        AuditTrailValidationException exception = new AuditTrailValidationException("Invalid actor", violations);

        assertThat(exception.getMessage()).isEqualTo("Invalid actor");
        assertThat(exception.getViolations()).anyMatch(v -> v.contains("Actor ID"));
    }

    @Test
    void shouldSupportEventValidation() {
        List<String> violations = Arrays.asList(
                "Event must have an actor",
                "Event must have an action",
                "Event must have a resource"
        );

        AuditTrailValidationException exception = new AuditTrailValidationException("Invalid event", violations);

        assertThat(exception.getViolations()).hasSize(3);
    }
}
