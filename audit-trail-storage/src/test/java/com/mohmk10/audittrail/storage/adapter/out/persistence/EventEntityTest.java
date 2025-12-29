package com.mohmk10.audittrail.storage.adapter.out.persistence;

import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventEntityTest {

    @Test
    void shouldMapAllFields() {
        EventEntity entity = new EventEntity();
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();

        entity.setId(id);
        entity.setTimestamp(timestamp);
        entity.setActorId("actor-123");
        entity.setActorType("USER");
        entity.setActorName("John Doe");
        entity.setActorIp("192.168.1.1");
        entity.setActorUserAgent("Mozilla/5.0");
        entity.setActorAttributes(Map.of("role", "admin"));
        entity.setActionType("CREATE");
        entity.setActionDescription("Created document");
        entity.setActionCategory("DOCUMENT");
        entity.setResourceId("res-456");
        entity.setResourceType("DOCUMENT");
        entity.setResourceName("Annual Report");
        entity.setResourceBefore(Map.of("status", "draft"));
        entity.setResourceAfter(Map.of("status", "published"));
        entity.setMetadataSource("web-app");
        entity.setTenantId("tenant-001");
        entity.setCorrelationId("corr-123");
        entity.setSessionId("session-abc");
        entity.setTags(Map.of("env", "test"));
        entity.setExtra(Map.of("custom", "value"));
        entity.setPreviousHash("prevHash");
        entity.setHash("currentHash");
        entity.setSignature("signature");
        entity.setCreatedAt(timestamp);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTimestamp()).isEqualTo(timestamp);
        assertThat(entity.getActorId()).isEqualTo("actor-123");
        assertThat(entity.getActorType()).isEqualTo("USER");
        assertThat(entity.getActorName()).isEqualTo("John Doe");
        assertThat(entity.getActorIp()).isEqualTo("192.168.1.1");
        assertThat(entity.getActorUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(entity.getActorAttributes()).containsEntry("role", "admin");
        assertThat(entity.getActionType()).isEqualTo("CREATE");
        assertThat(entity.getActionDescription()).isEqualTo("Created document");
        assertThat(entity.getActionCategory()).isEqualTo("DOCUMENT");
        assertThat(entity.getResourceId()).isEqualTo("res-456");
        assertThat(entity.getResourceType()).isEqualTo("DOCUMENT");
        assertThat(entity.getResourceName()).isEqualTo("Annual Report");
        assertThat(entity.getResourceBefore()).containsEntry("status", "draft");
        assertThat(entity.getResourceAfter()).containsEntry("status", "published");
        assertThat(entity.getMetadataSource()).isEqualTo("web-app");
        assertThat(entity.getTenantId()).isEqualTo("tenant-001");
        assertThat(entity.getCorrelationId()).isEqualTo("corr-123");
        assertThat(entity.getSessionId()).isEqualTo("session-abc");
        assertThat(entity.getTags()).containsEntry("env", "test");
        assertThat(entity.getExtra()).containsEntry("custom", "value");
        assertThat(entity.getPreviousHash()).isEqualTo("prevHash");
        assertThat(entity.getHash()).isEqualTo("currentHash");
        assertThat(entity.getSignature()).isEqualTo("signature");
        assertThat(entity.getCreatedAt()).isEqualTo(timestamp);
    }

    @Test
    void shouldHandleJsonbFields() {
        EventEntity entity = new EventEntity();

        Map<String, String> actorAttributes = Map.of("key1", "value1", "key2", "value2");
        Map<String, Object> resourceBefore = Map.of("field", "oldValue", "count", 10);
        Map<String, Object> resourceAfter = Map.of("field", "newValue", "count", 20);
        Map<String, String> tags = Map.of("tag1", "v1", "tag2", "v2");
        Map<String, Object> extra = Map.of("customField", "customValue");

        entity.setActorAttributes(actorAttributes);
        entity.setResourceBefore(resourceBefore);
        entity.setResourceAfter(resourceAfter);
        entity.setTags(tags);
        entity.setExtra(extra);

        assertThat(entity.getActorAttributes()).hasSize(2);
        assertThat(entity.getResourceBefore()).containsEntry("count", 10);
        assertThat(entity.getResourceAfter()).containsEntry("count", 20);
        assertThat(entity.getTags()).hasSize(2);
        assertThat(entity.getExtra()).containsEntry("customField", "customValue");
    }

    @Test
    void shouldPreserveTimestamps() {
        EventEntity entity = new EventEntity();
        Instant eventTimestamp = Instant.parse("2024-06-15T10:30:00Z");
        Instant createdAt = Instant.parse("2024-06-15T10:30:01Z");

        entity.setTimestamp(eventTimestamp);
        entity.setCreatedAt(createdAt);

        assertThat(entity.getTimestamp()).isEqualTo(eventTimestamp);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldHandleNullOptionalFields() {
        EventEntity entity = new EventEntity();
        entity.setId(UUID.randomUUID());
        entity.setTimestamp(Instant.now());
        entity.setActorId("actor");
        entity.setActorType("USER");
        entity.setActorName("Name");
        entity.setActionType("CREATE");
        entity.setResourceId("res");
        entity.setResourceType("DOCUMENT");
        entity.setResourceName("Name");
        entity.setTenantId("tenant");
        entity.setHash("hash");

        // These should be null
        assertThat(entity.getActorIp()).isNull();
        assertThat(entity.getActorUserAgent()).isNull();
        assertThat(entity.getActorAttributes()).isNull();
        assertThat(entity.getActionDescription()).isNull();
        assertThat(entity.getActionCategory()).isNull();
        assertThat(entity.getResourceBefore()).isNull();
        assertThat(entity.getResourceAfter()).isNull();
        assertThat(entity.getMetadataSource()).isNull();
        assertThat(entity.getCorrelationId()).isNull();
        assertThat(entity.getSessionId()).isNull();
        assertThat(entity.getTags()).isNull();
        assertThat(entity.getExtra()).isNull();
        assertThat(entity.getPreviousHash()).isNull();
        assertThat(entity.getSignature()).isNull();
    }

    @Test
    void shouldHandleEmptyCollections() {
        EventEntity entity = new EventEntity();

        entity.setActorAttributes(Map.of());
        entity.setResourceBefore(Map.of());
        entity.setResourceAfter(Map.of());
        entity.setTags(Map.of());
        entity.setExtra(Map.of());

        assertThat(entity.getActorAttributes()).isEmpty();
        assertThat(entity.getResourceBefore()).isEmpty();
        assertThat(entity.getResourceAfter()).isEmpty();
        assertThat(entity.getTags()).isEmpty();
        assertThat(entity.getExtra()).isEmpty();
    }

    @Test
    void shouldStoreUUID() {
        EventEntity entity = new EventEntity();
        UUID expectedId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        entity.setId(expectedId);

        assertThat(entity.getId()).isEqualTo(expectedId);
        assertThat(entity.getId().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void shouldStoreAllActorTypes() {
        EventEntity entity = new EventEntity();

        entity.setActorType("USER");
        assertThat(entity.getActorType()).isEqualTo("USER");

        entity.setActorType("SYSTEM");
        assertThat(entity.getActorType()).isEqualTo("SYSTEM");

        entity.setActorType("SERVICE");
        assertThat(entity.getActorType()).isEqualTo("SERVICE");
    }

    @Test
    void shouldStoreAllActionTypes() {
        EventEntity entity = new EventEntity();

        for (String actionType : List.of("CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "LOGOUT")) {
            entity.setActionType(actionType);
            assertThat(entity.getActionType()).isEqualTo(actionType);
        }
    }

    @Test
    void shouldStoreAllResourceTypes() {
        EventEntity entity = new EventEntity();

        for (String resourceType : List.of("DOCUMENT", "USER", "TRANSACTION", "CONFIG", "FILE", "API")) {
            entity.setResourceType(resourceType);
            assertThat(entity.getResourceType()).isEqualTo(resourceType);
        }
    }
}
