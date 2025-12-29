package com.mohmk10.audittrail.sdk.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTrailConnectionExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        AuditTrailConnectionException exception = new AuditTrailConnectionException("Connection failed");

        assertThat(exception.getMessage()).isEqualTo("Connection failed");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        IOException cause = new IOException("Network error");
        AuditTrailConnectionException exception = new AuditTrailConnectionException("Connection lost", cause);

        assertThat(exception.getMessage()).isEqualTo("Connection lost");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldExtendAuditTrailException() {
        AuditTrailConnectionException exception = new AuditTrailConnectionException("Test");

        assertThat(exception).isInstanceOf(AuditTrailException.class);
    }

    @Test
    void shouldWrapSocketTimeoutException() {
        SocketTimeoutException timeout = new SocketTimeoutException("Read timed out");
        AuditTrailConnectionException exception = new AuditTrailConnectionException("Request timeout", timeout);

        assertThat(exception.getCause()).isInstanceOf(SocketTimeoutException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("Read timed out");
    }

    @Test
    void shouldWrapInterruptedException() {
        InterruptedException interrupted = new InterruptedException("Thread interrupted");
        AuditTrailConnectionException exception = new AuditTrailConnectionException("Request interrupted", interrupted);

        assertThat(exception.getCause()).isInstanceOf(InterruptedException.class);
    }

    @Test
    void shouldSupportConnectionRefused() {
        Exception cause = new java.net.ConnectException("Connection refused");
        AuditTrailConnectionException exception = new AuditTrailConnectionException("Cannot connect to server", cause);

        assertThat(exception.getMessage()).isEqualTo("Cannot connect to server");
    }

    @Test
    void shouldSupportDnsResolutionFailure() {
        Exception cause = new java.net.UnknownHostException("Unknown host: invalid.example.com");
        AuditTrailConnectionException exception = new AuditTrailConnectionException("DNS resolution failed", cause);

        assertThat(exception.getCause()).isInstanceOf(java.net.UnknownHostException.class);
    }

    @Test
    void shouldPreserveCauseChain() {
        IOException rootCause = new IOException("Network unreachable");
        RuntimeException intermediate = new RuntimeException("Wrapped IO", rootCause);
        AuditTrailConnectionException exception = new AuditTrailConnectionException("Connection error", intermediate);

        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }
}
