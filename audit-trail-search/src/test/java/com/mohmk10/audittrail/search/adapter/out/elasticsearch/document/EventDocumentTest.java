package com.mohmk10.audittrail.search.adapter.out.elasticsearch.document;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventDocumentTest {

    @Test
    void shouldCreateEmptyDocument() {
        EventDocument document = new EventDocument();

        assertThat(document.getId()).isNull();
        assertThat(document.getTimestamp()).isNull();
        assertThat(document.getActorId()).isNull();
    }

    @Test
    void shouldSetAndGetAllFields() {
        EventDocument document = new EventDocument();
        Instant now = Instant.now();
        List<String> tags = List.of("env:prod", "region:us-east-1");

        document.setId("event-123");
        document.setTimestamp(now);
        document.setActorId("actor-123");
        document.setActorType("USER");
        document.setActorName("John Doe");
        document.setActorIp("192.168.1.1");
        document.setActionType("CREATE");
        document.setActionDescription("Created a document");
        document.setActionCategory("DOCUMENT");
        document.setResourceId("res-123");
        document.setResourceType("DOCUMENT");
        document.setResourceName("Annual Report");
        document.setSource("web-app");
        document.setTenantId("tenant-001");
        document.setCorrelationId("corr-123");
        document.setSessionId("session-abc");
        document.setTags(tags);
        document.setHash("hash-123");

        assertThat(document.getId()).isEqualTo("event-123");
        assertThat(document.getTimestamp()).isEqualTo(now);
        assertThat(document.getActorId()).isEqualTo("actor-123");
        assertThat(document.getActorType()).isEqualTo("USER");
        assertThat(document.getActorName()).isEqualTo("John Doe");
        assertThat(document.getActorIp()).isEqualTo("192.168.1.1");
        assertThat(document.getActionType()).isEqualTo("CREATE");
        assertThat(document.getActionDescription()).isEqualTo("Created a document");
        assertThat(document.getActionCategory()).isEqualTo("DOCUMENT");
        assertThat(document.getResourceId()).isEqualTo("res-123");
        assertThat(document.getResourceType()).isEqualTo("DOCUMENT");
        assertThat(document.getResourceName()).isEqualTo("Annual Report");
        assertThat(document.getSource()).isEqualTo("web-app");
        assertThat(document.getTenantId()).isEqualTo("tenant-001");
        assertThat(document.getCorrelationId()).isEqualTo("corr-123");
        assertThat(document.getSessionId()).isEqualTo("session-abc");
        assertThat(document.getTags()).isEqualTo(tags);
        assertThat(document.getHash()).isEqualTo("hash-123");
    }

    @Test
    void shouldHandleNullTags() {
        EventDocument document = new EventDocument();
        document.setTags(null);

        assertThat(document.getTags()).isNull();
    }

    @Test
    void shouldHandleEmptyTags() {
        EventDocument document = new EventDocument();
        document.setTags(List.of());

        assertThat(document.getTags()).isEmpty();
    }

    @Test
    void shouldPreserveTimestampPrecision() {
        EventDocument document = new EventDocument();
        Instant preciseTimestamp = Instant.parse("2024-06-15T10:30:45.123456789Z");

        document.setTimestamp(preciseTimestamp);

        assertThat(document.getTimestamp()).isEqualTo(preciseTimestamp);
    }

    @Test
    void shouldAllowUpdatingFields() {
        EventDocument document = new EventDocument();
        document.setActorId("actor-1");
        document.setActorId("actor-2");

        assertThat(document.getActorId()).isEqualTo("actor-2");
    }

    @Test
    void shouldStoreAllActorTypes() {
        for (String type : new String[]{"USER", "SYSTEM", "SERVICE"}) {
            EventDocument document = new EventDocument();
            document.setActorType(type);
            assertThat(document.getActorType()).isEqualTo(type);
        }
    }

    @Test
    void shouldStoreAllActionTypes() {
        for (String type : new String[]{"CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "LOGOUT"}) {
            EventDocument document = new EventDocument();
            document.setActionType(type);
            assertThat(document.getActionType()).isEqualTo(type);
        }
    }

    @Test
    void shouldStoreAllResourceTypes() {
        for (String type : new String[]{"DOCUMENT", "USER", "TRANSACTION", "CONFIG", "FILE", "API"}) {
            EventDocument document = new EventDocument();
            document.setResourceType(type);
            assertThat(document.getResourceType()).isEqualTo(type);
        }
    }

    @Test
    void shouldHandleMultipleTags() {
        EventDocument document = new EventDocument();
        List<String> tags = List.of("env:prod", "region:us-east-1", "team:backend", "version:1.0");

        document.setTags(tags);

        assertThat(document.getTags()).hasSize(4);
        assertThat(document.getTags()).contains("env:prod", "team:backend");
    }

    @Test
    void shouldStoreIpAddresses() {
        EventDocument document = new EventDocument();

        document.setActorIp("192.168.1.1");
        assertThat(document.getActorIp()).isEqualTo("192.168.1.1");

        document.setActorIp("10.0.0.1");
        assertThat(document.getActorIp()).isEqualTo("10.0.0.1");

        document.setActorIp("::1");
        assertThat(document.getActorIp()).isEqualTo("::1");
    }

    @Test
    void shouldStoreLongDescriptions() {
        EventDocument document = new EventDocument();
        String longDescription = "This is a very long action description that contains " +
                "detailed information about what action was performed, including " +
                "all the context and reasoning behind it.";

        document.setActionDescription(longDescription);

        assertThat(document.getActionDescription()).isEqualTo(longDescription);
    }

    @Test
    void shouldStoreHash() {
        EventDocument document = new EventDocument();
        String hash = "sha256:abc123def456789...";

        document.setHash(hash);

        assertThat(document.getHash()).isEqualTo(hash);
    }
}
