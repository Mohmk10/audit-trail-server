package com.mohmk10.audittrail.sdk;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditTrailClientBuilderTest {

    @Test
    void shouldBuildClientWithRequiredFields() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(client).isNotNull();
        assertThat(client).isInstanceOf(DefaultAuditTrailClient.class);
    }

    @Test
    void shouldThrowWhenServerUrlIsNull() {
        assertThatThrownBy(() -> AuditTrailClient.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Server URL is required");
    }

    @Test
    void shouldBuildClientWithApiKey() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080")
                .apiKey("atk_test_key")
                .build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldBuildClientWithCustomConnectTimeout() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080")
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldBuildClientWithCustomReadTimeout() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080")
                .readTimeout(Duration.ofSeconds(60))
                .build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldBuildClientWithCustomMaxRetries() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080")
                .maxRetries(5)
                .build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldBuildClientWithCustomRetryDelay() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080")
                .retryDelay(Duration.ofMillis(200))
                .build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldBuildClientWithAsyncDisabled() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080")
                .async(false)
                .build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldBuildFullyConfiguredClient() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("https://audit.example.com")
                .apiKey("atk_production_key")
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(45))
                .maxRetries(5)
                .retryDelay(Duration.ofMillis(200))
                .async(true)
                .build();

        assertThat(client).isNotNull();
        assertThat(client).isInstanceOf(DefaultAuditTrailClient.class);
    }

    @Test
    void shouldSupportFluentBuilderPattern() {
        AuditTrailClientBuilder builder = new AuditTrailClientBuilder()
                .serverUrl("http://localhost:8080")
                .apiKey("key")
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .maxRetries(3)
                .retryDelay(Duration.ofMillis(500))
                .async(true);

        AuditTrailClient client = builder.build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldAllowZeroRetries() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080")
                .maxRetries(0)
                .build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldCreateClientFromStaticBuilder() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080")
                .build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldSupportHttpsUrl() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("https://secure.audit-server.com:443")
                .build();

        assertThat(client).isNotNull();
    }

    @Test
    void shouldSupportUrlWithPath() {
        AuditTrailClient client = AuditTrailClient.builder()
                .serverUrl("http://localhost:8080/api/v1")
                .build();

        assertThat(client).isNotNull();
    }
}
