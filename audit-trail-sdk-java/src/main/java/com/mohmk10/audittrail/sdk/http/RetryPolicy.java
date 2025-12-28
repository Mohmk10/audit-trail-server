package com.mohmk10.audittrail.sdk.http;

import java.time.Duration;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mohmk10.audittrail.sdk.exception.AuditTrailApiException;
import com.mohmk10.audittrail.sdk.exception.AuditTrailConnectionException;

public class RetryPolicy {
    private static final Logger log = LoggerFactory.getLogger(RetryPolicy.class);

    private final int maxRetries;
    private final Duration retryDelay;

    public RetryPolicy(int maxRetries, Duration retryDelay) {
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }

    public <T> T execute(Supplier<T> operation) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt <= maxRetries) {
            try {
                return operation.get();
            } catch (AuditTrailApiException e) {
                if (e.isServerError() && attempt < maxRetries) {
                    lastException = e;
                    attempt++;
                    waitBeforeRetry(attempt);
                    log.debug("Retry attempt {} after server error: {}", attempt, e.getMessage());
                } else {
                    throw e;
                }
            } catch (AuditTrailConnectionException e) {
                if (attempt < maxRetries) {
                    lastException = e;
                    attempt++;
                    waitBeforeRetry(attempt);
                    log.debug("Retry attempt {} after connection error: {}", attempt, e.getMessage());
                } else {
                    throw e;
                }
            }
        }

        throw new AuditTrailConnectionException("Max retries exceeded", lastException);
    }

    private void waitBeforeRetry(int attempt) {
        try {
            long delayMs = retryDelay.toMillis() * (long) Math.pow(2, attempt - 1);
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuditTrailConnectionException("Retry interrupted", e);
        }
    }
}
