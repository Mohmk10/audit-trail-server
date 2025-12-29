package com.mohmk10.audittrail.integration.exporter.splunk;

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

@DisplayName("SplunkExporter Tests")
class SplunkExporterTest {

    private MockWebServer mockWebServer;
    private SplunkExporter exporter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        SplunkProperties properties = new SplunkProperties();
        properties.setEnabled(true);
        properties.setUrl(mockWebServer.url("").toString());
        properties.setToken("test-token");
        properties.setIndex("audit_events");
        properties.setSourceType("audit_trail");
        properties.setTimeoutSeconds(30);

        exporter = new SplunkExporter(properties, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("getName() Tests")
    class GetNameTests {

        @Test
        @DisplayName("Should return 'splunk'")
        void shouldReturnSplunk() {
            assertThat(exporter.getName()).isEqualTo("splunk");
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

        @Test
        @DisplayName("Should return false when disabled")
        void shouldReturnFalseWhenDisabled() throws IOException {
            SplunkProperties properties = new SplunkProperties();
            properties.setEnabled(false);
            SplunkExporter disabledExporter = new SplunkExporter(properties, objectMapper);

            assertThat(disabledExporter.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("export() Tests")
    class ExportTests {

        @Test
        @DisplayName("Should export event successfully")
        void shouldExportEventSuccessfully() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"text\":\"Success\"}"));

            Event event = createEvent();

            ExportResult result = exporter.export(event).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(1);
            assertThat(result.exporterName()).isEqualTo("splunk");

            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getPath()).isEqualTo("/services/collector/event");
            assertThat(request.getHeader("Authorization")).isEqualTo("Splunk test-token");
        }

        @Test
        @DisplayName("Should return failure on HTTP error")
        void shouldReturnFailureOnHttpError() throws Exception {
            mockWebServer.enqueue(new MockResponse().setResponseCode(500));

            Event event = createEvent();

            ExportResult result = exporter.export(event).get();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should include event data in request")
        void shouldIncludeEventDataInRequest() throws Exception {
            mockWebServer.enqueue(new MockResponse().setResponseCode(200));

            Event event = new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("user@example.com", Actor.ActorType.USER, "User", null, null, null),
                new Action(Action.ActionType.LOGIN, "login", "security"),
                new Resource("session-1", Resource.ResourceType.SYSTEM, "Session", null, null),
                null,
                null,
                null,
                null
            );

            exporter.export(event).get();

            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            String body = request.getBody().readUtf8();
            assertThat(body).contains("user@example.com");
            assertThat(body).contains("LOGIN");
        }
    }

    @Nested
    @DisplayName("exportBatch() Tests")
    class ExportBatchTests {

        @Test
        @DisplayName("Should export batch successfully")
        void shouldExportBatchSuccessfully() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"text\":\"Success\"}"));

            List<Event> events = List.of(
                createEvent(),
                createEvent(),
                createEvent()
            );

            ExportResult result = exporter.exportBatch(events).get(5, TimeUnit.SECONDS);

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(3);
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
