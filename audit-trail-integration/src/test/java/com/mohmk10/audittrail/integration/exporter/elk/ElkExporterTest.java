package com.mohmk10.audittrail.integration.exporter.elk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.core.domain.*;
import com.mohmk10.audittrail.integration.exporter.ExportResult;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ElkExporter Tests")
class ElkExporterTest {

    private MockWebServer mockWebServer;
    private ElkExporter exporter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ElkProperties properties = new ElkProperties();
        properties.setEnabled(true);
        properties.setUrl(mockWebServer.url("").toString());
        properties.setIndexPrefix("audit-events");
        properties.setTimeoutSeconds(30);

        exporter = new ElkExporter(properties, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("getName() Tests")
    class GetNameTests {

        @Test
        @DisplayName("Should return 'elk'")
        void shouldReturnElk() {
            assertThat(exporter.getName()).isEqualTo("elk");
        }
    }

    @Nested
    @DisplayName("isEnabled() Tests")
    class IsEnabledTests {

        @Test
        @DisplayName("Should return true when enabled")
        void shouldReturnTrueWhenEnabled() {
            assertThat(exporter.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("export() Tests")
    class ExportTests {

        @Test
        @DisplayName("Should export single event successfully")
        void shouldExportSingleEventSuccessfully() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("{\"result\":\"created\"}"));

            Event event = createEvent();

            ExportResult result = exporter.export(event).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(1);

            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getPath()).contains("/_doc/");
        }

        @Test
        @DisplayName("Should use date-based index name")
        void shouldUseDateBasedIndexName() throws Exception {
            mockWebServer.enqueue(new MockResponse().setResponseCode(201));

            Event event = new Event(
                UUID.randomUUID(),
                Instant.parse("2024-06-15T12:00:00Z"),
                new Actor("user", Actor.ActorType.USER, "User", null, null, null),
                new Action(Action.ActionType.READ, "action", "security"),
                new Resource("resource-1", Resource.ResourceType.DOCUMENT, "Resource", null, null),
                null,
                null,
                null,
                null
            );

            exporter.export(event).get();

            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getPath()).contains("audit-events-2024.06.15");
        }

        @Test
        @DisplayName("Should return failure on HTTP error")
        void shouldReturnFailureOnHttpError() throws Exception {
            mockWebServer.enqueue(new MockResponse().setResponseCode(500));

            Event event = createEvent();

            ExportResult result = exporter.export(event).get();

            assertThat(result.success()).isFalse();
        }
    }

    @Nested
    @DisplayName("exportBatch() Tests")
    class ExportBatchTests {

        @Test
        @DisplayName("Should export batch using bulk API")
        void shouldExportBatchUsingBulkApi() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"errors\":false}"));

            List<Event> events = List.of(
                createEvent(),
                createEvent()
            );

            ExportResult result = exporter.exportBatch(events).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(2);

            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getPath()).isEqualTo("/_bulk");
        }

        @Test
        @DisplayName("Should format bulk request correctly")
        void shouldFormatBulkRequestCorrectly() throws Exception {
            mockWebServer.enqueue(new MockResponse().setResponseCode(200));

            List<Event> events = List.of(createEvent());

            exporter.exportBatch(events).get();

            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            String body = request.getBody().readUtf8();
            // Bulk format: action line + document line
            String[] lines = body.split("\n");
            assertThat(lines.length).isGreaterThanOrEqualTo(2);
            assertThat(lines[0]).contains("index");
        }

        @Test
        @DisplayName("Should handle empty list")
        void shouldHandleEmptyList() throws Exception {
            ExportResult result = exporter.exportBatch(List.of()).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isZero();
        }
    }

    private Event createEvent() {
        return new Event(
            UUID.randomUUID(),
            Instant.now(),
            new Actor("test-user", Actor.ActorType.USER, "Test User", "127.0.0.1", null, Map.of()),
            new Action(Action.ActionType.READ, "test-action", "security"),
            new Resource("resource-1", Resource.ResourceType.DOCUMENT, "Test Resource", null, null),
            null,
            null,
            null,
            null
        );
    }
}
