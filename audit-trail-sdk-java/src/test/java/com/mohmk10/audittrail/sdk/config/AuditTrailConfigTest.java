package com.mohmk10.audittrail.sdk.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditTrailConfigTest {

    @Test
    void shouldCreateConfigWithRequiredFields() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(config.getServerUrl()).isEqualTo("http://localhost:8080");
    }

    @Test
    void shouldThrowWhenServerUrlIsNull() {
        assertThatThrownBy(() -> AuditTrailConfig.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Server URL is required");
    }

    @Test
    void shouldUseDefaultConnectTimeout() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(config.getConnectTimeout()).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void shouldUseDefaultReadTimeout() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(config.getReadTimeout()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void shouldUseDefaultMaxRetries() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(config.getMaxRetries()).isEqualTo(3);
    }

    @Test
    void shouldUseDefaultRetryDelay() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(config.getRetryDelay()).isEqualTo(Duration.ofMillis(500));
    }

    @Test
    void shouldUseDefaultAsyncEnabled() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(config.isAsyncEnabled()).isTrue();
    }

    @Test
    void shouldUseDefaultBatchSize() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(config.getBatchSize()).isEqualTo(100);
    }

    @Test
    void shouldUseDefaultBatchFlushInterval() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(config.getBatchFlushInterval()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void shouldSetApiKey() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .apiKey("atk_test_key_123")
                .build();

        assertThat(config.getApiKey()).isEqualTo("atk_test_key_123");
    }

    @Test
    void shouldSetCustomConnectTimeout() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        assertThat(config.getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void shouldSetCustomReadTimeout() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .readTimeout(Duration.ofSeconds(60))
                .build();

        assertThat(config.getReadTimeout()).isEqualTo(Duration.ofSeconds(60));
    }

    @Test
    void shouldSetCustomMaxRetries() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .maxRetries(5)
                .build();

        assertThat(config.getMaxRetries()).isEqualTo(5);
    }

    @Test
    void shouldSetCustomRetryDelay() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .retryDelay(Duration.ofSeconds(1))
                .build();

        assertThat(config.getRetryDelay()).isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    void shouldSetAsyncDisabled() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .asyncEnabled(false)
                .build();

        assertThat(config.isAsyncEnabled()).isFalse();
    }

    @Test
    void shouldSetCustomBatchSize() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .batchSize(50)
                .build();

        assertThat(config.getBatchSize()).isEqualTo(50);
    }

    @Test
    void shouldSetCustomBatchFlushInterval() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .batchFlushInterval(Duration.ofSeconds(10))
                .build();

        assertThat(config.getBatchFlushInterval()).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void shouldCreateFullyCustomConfig() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("https://audit.example.com")
                .apiKey("atk_production_key")
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(45))
                .maxRetries(5)
                .retryDelay(Duration.ofMillis(200))
                .asyncEnabled(true)
                .batchSize(200)
                .batchFlushInterval(Duration.ofSeconds(3))
                .build();

        assertThat(config.getServerUrl()).isEqualTo("https://audit.example.com");
        assertThat(config.getApiKey()).isEqualTo("atk_production_key");
        assertThat(config.getConnectTimeout()).isEqualTo(Duration.ofSeconds(15));
        assertThat(config.getReadTimeout()).isEqualTo(Duration.ofSeconds(45));
        assertThat(config.getMaxRetries()).isEqualTo(5);
        assertThat(config.getRetryDelay()).isEqualTo(Duration.ofMillis(200));
        assertThat(config.isAsyncEnabled()).isTrue();
        assertThat(config.getBatchSize()).isEqualTo(200);
        assertThat(config.getBatchFlushInterval()).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    void shouldAllowZeroRetries() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .maxRetries(0)
                .build();

        assertThat(config.getMaxRetries()).isZero();
    }

    @Test
    void shouldAllowBatchSizeOfOne() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .batchSize(1)
                .build();

        assertThat(config.getBatchSize()).isEqualTo(1);
    }

    @Test
    void shouldAllowNullApiKey() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080")
                .apiKey(null)
                .build();

        assertThat(config.getApiKey()).isNull();
    }

    @Test
    void shouldSupportHttpsUrl() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("https://secure.audit-server.com:443")
                .build();

        assertThat(config.getServerUrl()).startsWith("https://");
    }

    @Test
    void shouldSupportUrlWithPath() {
        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl("http://localhost:8080/api/v1")
                .build();

        assertThat(config.getServerUrl()).isEqualTo("http://localhost:8080/api/v1");
    }
}
