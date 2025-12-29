package com.mohmk10.audittrail.sdk.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTrailExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        AuditTrailException exception = new AuditTrailException("Test error message");

        assertThat(exception.getMessage()).isEqualTo("Test error message");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        Throwable cause = new RuntimeException("Original error");
        AuditTrailException exception = new AuditTrailException("Wrapped error", cause);

        assertThat(exception.getMessage()).isEqualTo("Wrapped error");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Original error");
    }

    @Test
    void shouldBeRuntimeException() {
        AuditTrailException exception = new AuditTrailException("Test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldPreserveCauseStackTrace() {
        Throwable cause = new IllegalArgumentException("Invalid argument");
        AuditTrailException exception = new AuditTrailException("Wrapper", cause);

        assertThat(exception.getCause().getStackTrace()).isNotEmpty();
    }

    @Test
    void shouldSupportNestedCause() {
        Throwable rootCause = new NullPointerException("Null value");
        Throwable intermediateCause = new IllegalStateException("Bad state", rootCause);
        AuditTrailException exception = new AuditTrailException("Top level error", intermediateCause);

        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }

    @Test
    void shouldHaveStackTrace() {
        AuditTrailException exception = new AuditTrailException("Test");

        assertThat(exception.getStackTrace()).isNotEmpty();
    }

    @Test
    void shouldSupportEmptyMessage() {
        AuditTrailException exception = new AuditTrailException("");

        assertThat(exception.getMessage()).isEmpty();
    }

    @Test
    void shouldSupportNullCause() {
        AuditTrailException exception = new AuditTrailException("Message", null);

        assertThat(exception.getMessage()).isEqualTo("Message");
        assertThat(exception.getCause()).isNull();
    }
}
