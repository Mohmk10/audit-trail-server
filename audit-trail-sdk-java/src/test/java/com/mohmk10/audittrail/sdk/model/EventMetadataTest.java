package com.mohmk10.audittrail.sdk.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EventMetadataTest {

    @Test
    void shouldCreateMetadataWithBuilder() {
        EventMetadata metadata = EventMetadata.builder()
                .source("web-app")
                .tenantId("tenant-001")
                .build();

        assertThat(metadata.getSource()).isEqualTo("web-app");
        assertThat(metadata.getTenantId()).isEqualTo("tenant-001");
    }

    @Test
    void shouldCreateMetadataWithCorrelationId() {
        EventMetadata metadata = EventMetadata.builder()
                .correlationId("corr-12345")
                .build();

        assertThat(metadata.getCorrelationId()).isEqualTo("corr-12345");
    }

    @Test
    void shouldCreateMetadataWithSessionId() {
        EventMetadata metadata = EventMetadata.builder()
                .sessionId("session-abc123")
                .build();

        assertThat(metadata.getSessionId()).isEqualTo("session-abc123");
    }

    @Test
    void shouldAddSingleTag() {
        EventMetadata metadata = EventMetadata.builder()
                .tag("environment", "production")
                .build();

        assertThat(metadata.getTags()).containsEntry("environment", "production");
    }

    @Test
    void shouldAddMultipleTags() {
        EventMetadata metadata = EventMetadata.builder()
                .tag("env", "prod")
                .tag("region", "us-east")
                .tag("version", "1.0.0")
                .build();

        assertThat(metadata.getTags()).hasSize(3);
        assertThat(metadata.getTags()).containsEntry("env", "prod");
        assertThat(metadata.getTags()).containsEntry("region", "us-east");
        assertThat(metadata.getTags()).containsEntry("version", "1.0.0");
    }

    @Test
    void shouldAddTagsFromMap() {
        Map<String, String> tags = Map.of("key1", "value1", "key2", "value2");

        EventMetadata metadata = EventMetadata.builder()
                .tags(tags)
                .build();

        assertThat(metadata.getTags()).containsAllEntriesOf(tags);
    }

    @Test
    void shouldAddSingleExtra() {
        EventMetadata metadata = EventMetadata.builder()
                .extra("requestId", "req-123")
                .build();

        assertThat(metadata.getExtra()).containsEntry("requestId", "req-123");
    }

    @Test
    void shouldAddMultipleExtras() {
        EventMetadata metadata = EventMetadata.builder()
                .extra("requestId", "req-123")
                .extra("userId", 12345)
                .extra("isAdmin", true)
                .build();

        assertThat(metadata.getExtra()).hasSize(3);
        assertThat(metadata.getExtra()).containsEntry("requestId", "req-123");
        assertThat(metadata.getExtra()).containsEntry("userId", 12345);
        assertThat(metadata.getExtra()).containsEntry("isAdmin", true);
    }

    @Test
    void shouldAddExtrasFromMap() {
        Map<String, Object> extra = Map.of("key1", "value1", "key2", 123);

        EventMetadata metadata = EventMetadata.builder()
                .extra(extra)
                .build();

        assertThat(metadata.getExtra()).containsAllEntriesOf(extra);
    }

    @Test
    void shouldReturnNullForEmptyTags() {
        EventMetadata metadata = EventMetadata.builder()
                .source("test")
                .build();

        assertThat(metadata.getTags()).isNull();
    }

    @Test
    void shouldReturnNullForEmptyExtras() {
        EventMetadata metadata = EventMetadata.builder()
                .source("test")
                .build();

        assertThat(metadata.getExtra()).isNull();
    }

    @Test
    void shouldHaveNullFieldsWhenNotSet() {
        EventMetadata metadata = EventMetadata.builder().build();

        assertThat(metadata.getSource()).isNull();
        assertThat(metadata.getTenantId()).isNull();
        assertThat(metadata.getCorrelationId()).isNull();
        assertThat(metadata.getSessionId()).isNull();
    }

    @Test
    void shouldSupportEqualsBasedOnSourceAndTenantId() {
        EventMetadata metadata1 = EventMetadata.builder()
                .source("app1")
                .tenantId("tenant1")
                .correlationId("corr1")
                .build();

        EventMetadata metadata2 = EventMetadata.builder()
                .source("app1")
                .tenantId("tenant1")
                .correlationId("corr2")
                .build();

        assertThat(metadata1).isEqualTo(metadata2);
        assertThat(metadata1.hashCode()).isEqualTo(metadata2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentSource() {
        EventMetadata metadata1 = EventMetadata.builder()
                .source("app1")
                .tenantId("tenant1")
                .build();

        EventMetadata metadata2 = EventMetadata.builder()
                .source("app2")
                .tenantId("tenant1")
                .build();

        assertThat(metadata1).isNotEqualTo(metadata2);
    }

    @Test
    void shouldNotBeEqualWithDifferentTenantId() {
        EventMetadata metadata1 = EventMetadata.builder()
                .source("app1")
                .tenantId("tenant1")
                .build();

        EventMetadata metadata2 = EventMetadata.builder()
                .source("app1")
                .tenantId("tenant2")
                .build();

        assertThat(metadata1).isNotEqualTo(metadata2);
    }

    @Test
    void shouldCreateCompleteMetadata() {
        Map<String, String> tags = Map.of("env", "prod");
        Map<String, Object> extra = Map.of("version", "2.0");

        EventMetadata metadata = EventMetadata.builder()
                .source("payment-service")
                .tenantId("acme-corp")
                .correlationId("corr-uuid")
                .sessionId("sess-uuid")
                .tags(tags)
                .extra(extra)
                .build();

        assertThat(metadata.getSource()).isEqualTo("payment-service");
        assertThat(metadata.getTenantId()).isEqualTo("acme-corp");
        assertThat(metadata.getCorrelationId()).isEqualTo("corr-uuid");
        assertThat(metadata.getSessionId()).isEqualTo("sess-uuid");
        assertThat(metadata.getTags()).isEqualTo(tags);
        assertThat(metadata.getExtra()).isEqualTo(extra);
    }

    @Test
    void shouldMergeTagsFromMultipleSources() {
        Map<String, String> tags1 = Map.of("key1", "value1");

        EventMetadata metadata = EventMetadata.builder()
                .tags(tags1)
                .tag("key2", "value2")
                .build();

        assertThat(metadata.getTags()).hasSize(2);
        assertThat(metadata.getTags()).containsEntry("key1", "value1");
        assertThat(metadata.getTags()).containsEntry("key2", "value2");
    }

    @Test
    void shouldMergeExtrasFromMultipleSources() {
        Map<String, Object> extra1 = Map.of("key1", "value1");

        EventMetadata metadata = EventMetadata.builder()
                .extra(extra1)
                .extra("key2", "value2")
                .build();

        assertThat(metadata.getExtra()).hasSize(2);
        assertThat(metadata.getExtra()).containsEntry("key1", "value1");
        assertThat(metadata.getExtra()).containsEntry("key2", "value2");
    }
}
