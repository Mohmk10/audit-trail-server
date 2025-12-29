package com.mohmk10.audittrail.sdk.http;

import com.mohmk10.audittrail.sdk.exception.AuditTrailApiException;
import com.mohmk10.audittrail.sdk.exception.AuditTrailConnectionException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryPolicyTest {

    @Test
    void shouldReturnResultOnFirstSuccess() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        String result = policy.execute(() -> {
            attempts.incrementAndGet();
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void shouldRetryOnServerError() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        String result = policy.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 2) {
                throw new AuditTrailApiException(500, "Server error");
            }
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void shouldRetryOnConnectionError() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        String result = policy.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new AuditTrailConnectionException("Connection failed");
            }
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldThrowAfterMaxRetriesOnServerError() {
        RetryPolicy policy = new RetryPolicy(2, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new AuditTrailApiException(503, "Service unavailable");
        }))
                .isInstanceOf(AuditTrailApiException.class)
                .hasMessageContaining("Service unavailable");

        assertThat(attempts.get()).isEqualTo(3); // initial + 2 retries
    }

    @Test
    void shouldThrowAfterMaxRetriesOnConnectionError() {
        RetryPolicy policy = new RetryPolicy(2, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new AuditTrailConnectionException("Connection timeout");
        }))
                .isInstanceOf(AuditTrailConnectionException.class)
                .hasMessageContaining("Connection timeout");

        assertThat(attempts.get()).isEqualTo(3); // initial + 2 retries
    }

    @Test
    void shouldNotRetryOnClientError() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new AuditTrailApiException(400, "Bad request");
        }))
                .isInstanceOf(AuditTrailApiException.class)
                .hasMessageContaining("Bad request");

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void shouldNotRetryOn401Error() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new AuditTrailApiException(401, "Unauthorized");
        }))
                .isInstanceOf(AuditTrailApiException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void shouldNotRetryOn403Error() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new AuditTrailApiException(403, "Forbidden");
        }))
                .isInstanceOf(AuditTrailApiException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void shouldNotRetryOn404Error() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new AuditTrailApiException(404, "Not found");
        }))
                .isInstanceOf(AuditTrailApiException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void shouldRetryOn502Error() {
        RetryPolicy policy = new RetryPolicy(2, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        String result = policy.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 2) {
                throw new AuditTrailApiException(502, "Bad gateway");
            }
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void shouldWorkWithZeroRetries() {
        RetryPolicy policy = new RetryPolicy(0, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new AuditTrailApiException(500, "Server error");
        }))
                .isInstanceOf(AuditTrailApiException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void shouldRecoverAfterMultipleFailures() {
        RetryPolicy policy = new RetryPolicy(5, Duration.ofMillis(10));
        AtomicInteger attempts = new AtomicInteger(0);

        String result = policy.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 4) {
                throw new AuditTrailConnectionException("Temporary failure");
            }
            return "recovered";
        });

        assertThat(result).isEqualTo("recovered");
        assertThat(attempts.get()).isEqualTo(4);
    }

    @Test
    void shouldHandleNullReturnValue() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(10));

        String result = policy.execute(() -> null);

        assertThat(result).isNull();
    }

    @Test
    void shouldRetryWithExponentialBackoff() {
        RetryPolicy policy = new RetryPolicy(2, Duration.ofMillis(50));
        AtomicInteger attempts = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        assertThatThrownBy(() -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new AuditTrailConnectionException("Connection failed");
        }));

        long elapsed = System.currentTimeMillis() - startTime;
        // Should wait at least: 50ms (first retry) + 100ms (second retry) = 150ms
        assertThat(elapsed).isGreaterThanOrEqualTo(100);
    }
}
