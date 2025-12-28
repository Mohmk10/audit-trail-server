package com.mohmk10.audittrail.sdk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.mohmk10.audittrail.sdk.model.Action;
import com.mohmk10.audittrail.sdk.model.Actor;
import com.mohmk10.audittrail.sdk.model.Event;
import com.mohmk10.audittrail.sdk.model.EventMetadata;
import com.mohmk10.audittrail.sdk.model.EventResponse;
import com.mohmk10.audittrail.sdk.model.Resource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditTrailClientIntegrationTest {
    private static final String SERVER_URL = "http://localhost:8080";
    private static final String TENANT_ID = "tenant-sdk-test";
    private static final String SOURCE = "sdk-integration-test";
    private AuditTrailClient client;

    @BeforeAll
    void setup() {
        client = AuditTrailClient.builder()
                .serverUrl(SERVER_URL)
                .apiKey("test-key")
                .build();
    }

    private EventMetadata defaultMetadata() {
        return EventMetadata.builder()
                .source(SOURCE)
                .tenantId(TENANT_ID)
                .build();
    }

    @Test
    void shouldLogEventSuccessfully() {
        EventResponse response = client.log(Event.builder()
                .actor(Actor.user("sdk-test-user", "SDK Tester"))
                .action(Action.create("SDK integration test"))
                .resource(Resource.document("doc-sdk-001", "Test Document"))
                .metadata(defaultMetadata())
                .build());

        assertNotNull(response.getId());
        assertNotNull(response.getHash());
        assertEquals("STORED", response.getStatus());
    }

    @Test
    void shouldLogBatchSuccessfully() {
        List<Event> events = List.of(
                Event.builder()
                        .actor(Actor.user("batch-user-1", "Batch User 1"))
                        .action(Action.create("Batch event 1"))
                        .resource(Resource.document("batch-doc-1", "Batch Doc 1"))
                        .metadata(defaultMetadata())
                        .build(),
                Event.builder()
                        .actor(Actor.user("batch-user-2", "Batch User 2"))
                        .action(Action.update("Batch event 2"))
                        .resource(Resource.document("batch-doc-2", "Batch Doc 2"))
                        .metadata(defaultMetadata())
                        .build()
        );

        List<EventResponse> responses = client.logBatch(events);

        assertEquals(2, responses.size());
        responses.forEach(r -> {
            assertNotNull(r.getId());
            assertNotNull(r.getHash());
            assertEquals("STORED", r.getStatus());
        });
    }

    @Test
    void shouldFindEventById() {
        EventResponse created = client.log(Event.builder()
                .actor(Actor.system("find-test-system"))
                .action(Action.read("Find by ID test"))
                .resource(Resource.config("config-find-001", "Test Config"))
                .metadata(defaultMetadata())
                .build());

        Optional<EventResponse> found = client.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(created.getHash(), found.get().getHash());
    }

    @Test
    void shouldLogAsyncSuccessfully() throws Exception {
        CompletableFuture<EventResponse> future = client.logAsync(Event.builder()
                .actor(Actor.service("async-service", "Async Service"))
                .action(Action.create("Async operation test"))
                .resource(Resource.of("async-resource", "API", "Async Resource"))
                .metadata(EventMetadata.builder()
                        .source(SOURCE)
                        .tenantId(TENANT_ID)
                        .correlationId(UUID.randomUUID().toString())
                        .build())
                .build());

        EventResponse response = future.get();

        assertNotNull(response.getId());
        assertNotNull(response.getHash());
        assertEquals("STORED", response.getStatus());
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<EventResponse> result = client.findById(nonExistentId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUseStaticFactoryMethods() {
        EventResponse response = client.log(Event.builder()
                .actor(Actor.user("factory-user", "Factory User").toBuilder()
                        .ip("192.168.1.100")
                        .build())
                .action(Action.delete("Delete operation"))
                .resource(Resource.file("file-001", "Important File"))
                .metadata(defaultMetadata())
                .build());

        assertNotNull(response.getId());
        assertEquals("STORED", response.getStatus());
    }

    @Test
    void shouldLogWithBeforeAfterState() {
        EventResponse response = client.log(Event.builder()
                .actor(Actor.user("state-user", "State User"))
                .action(Action.update("Updated document content"))
                .resource(Resource.builder()
                        .id("doc-state-001")
                        .type("DOCUMENT")
                        .name("Stateful Document")
                        .before("title", "Old Title")
                        .before("version", 1)
                        .after("title", "New Title")
                        .after("version", 2)
                        .build())
                .metadata(EventMetadata.builder()
                        .source(SOURCE)
                        .tenantId(TENANT_ID)
                        .tag("environment", "test")
                        .tag("module", "sdk")
                        .build())
                .build());

        assertNotNull(response.getId());
        assertEquals("STORED", response.getStatus());
    }
}
