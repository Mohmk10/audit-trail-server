package com.mohmk10.audittrail.core.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EventMetadataTest {

    @Test
    void shouldCreateMetadataWithRequiredFields() {
        EventMetadata metadata = new EventMetadata(
                "web-application",
                "tenant-001",
                null,
                null,
                null,
                null
        );

        assertThat(metadata.source()).isEqualTo("web-application");
        assertThat(metadata.tenantId()).isEqualTo("tenant-001");
    }

    @Test
    void shouldCreateMetadataWithAllFields() {
        Map<String, String> tags = Map.of("env", "production", "region", "us-east");
        Map<String, Object> extra = Map.of("customField", "customValue", "count", 42);

        EventMetadata metadata = new EventMetadata(
                "api-gateway",
                "tenant-002",
                "corr-abc-123",
                "session-xyz-789",
                tags,
                extra
        );

        assertThat(metadata.source()).isEqualTo("api-gateway");
        assertThat(metadata.tenantId()).isEqualTo("tenant-002");
        assertThat(metadata.correlationId()).isEqualTo("corr-abc-123");
        assertThat(metadata.sessionId()).isEqualTo("session-xyz-789");
        assertThat(metadata.tags()).isEqualTo(tags);
        assertThat(metadata.extra()).isEqualTo(extra);
    }

    @Test
    void shouldHandleCorrelationId() {
        EventMetadata metadata = new EventMetadata(
                "source",
                "tenant",
                "correlation-id-12345",
                null,
                null,
                null
        );

        assertThat(metadata.correlationId()).isEqualTo("correlation-id-12345");
    }

    @Test
    void shouldHandleSessionId() {
        EventMetadata metadata = new EventMetadata(
                "source",
                "tenant",
                null,
                "session-abcdef",
                null,
                null
        );

        assertThat(metadata.sessionId()).isEqualTo("session-abcdef");
    }

    @Test
    void shouldHandleTags() {
        Map<String, String> tags = Map.of(
                "priority", "high",
                "category", "security",
                "team", "platform"
        );

        EventMetadata metadata = new EventMetadata(
                "source",
                "tenant",
                null,
                null,
                tags,
                null
        );

        assertThat(metadata.tags()).hasSize(3);
        assertThat(metadata.tags()).containsEntry("priority", "high");
        assertThat(metadata.tags()).containsEntry("category", "security");
        assertThat(metadata.tags()).containsEntry("team", "platform");
    }

    @Test
    void shouldHandleExtraData() {
        Map<String, Object> extra = Map.of(
                "requestSize", 1024,
                "responseTime", 250,
                "success", true,
                "endpoint", "/api/v1/events"
        );

        EventMetadata metadata = new EventMetadata(
                "source",
                "tenant",
                null,
                null,
                null,
                extra
        );

        assertThat(metadata.extra()).hasSize(4);
        assertThat(metadata.extra()).containsEntry("requestSize", 1024);
        assertThat(metadata.extra()).containsEntry("responseTime", 250);
        assertThat(metadata.extra()).containsEntry("success", true);
        assertThat(metadata.extra()).containsEntry("endpoint", "/api/v1/events");
    }

    @Test
    void shouldHandleNullOptionalFields() {
        EventMetadata metadata = new EventMetadata(
                "source",
                "tenant",
                null,
                null,
                null,
                null
        );

        assertThat(metadata.correlationId()).isNull();
        assertThat(metadata.sessionId()).isNull();
        assertThat(metadata.tags()).isNull();
        assertThat(metadata.extra()).isNull();
    }

    @Test
    void shouldHandleEmptyCollections() {
        EventMetadata metadata = new EventMetadata(
                "source",
                "tenant",
                null,
                null,
                Map.of(),
                Map.of()
        );

        assertThat(metadata.tags()).isEmpty();
        assertThat(metadata.extra()).isEmpty();
    }

    @Test
    void shouldSupportRecordEquality() {
        EventMetadata metadata1 = new EventMetadata("src", "t1", "c1", "s1", Map.of(), Map.of());
        EventMetadata metadata2 = new EventMetadata("src", "t1", "c1", "s1", Map.of(), Map.of());

        assertThat(metadata1).isEqualTo(metadata2);
        assertThat(metadata1.hashCode()).isEqualTo(metadata2.hashCode());
    }

    @Test
    void shouldDifferentiateNonEqualMetadata() {
        EventMetadata metadata1 = new EventMetadata("src", "tenant1", null, null, null, null);
        EventMetadata metadata2 = new EventMetadata("src", "tenant2", null, null, null, null);

        assertThat(metadata1).isNotEqualTo(metadata2);
    }
}
