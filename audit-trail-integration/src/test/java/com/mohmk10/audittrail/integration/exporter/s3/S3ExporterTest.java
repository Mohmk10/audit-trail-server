package com.mohmk10.audittrail.integration.exporter.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.core.domain.*;
import com.mohmk10.audittrail.integration.exporter.ExportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3Exporter Tests")
class S3ExporterTest {

    @Mock
    private S3AsyncClient s3Client;

    private S3Exporter exporter;
    private ObjectMapper objectMapper;
    private S3Properties properties;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        properties = new S3Properties();
        properties.setEnabled(true);
        properties.setBucket("test-bucket");
        properties.setPrefix("audit-events");
        properties.setRegion("us-east-1");

        exporter = new S3Exporter(s3Client, properties, objectMapper);
    }

    @Nested
    @DisplayName("getName() Tests")
    class GetNameTests {

        @Test
        @DisplayName("Should return 's3'")
        void shouldReturnS3() {
            assertThat(exporter.getName()).isEqualTo("s3");
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
        void shouldReturnFalseWhenDisabled() {
            S3Properties disabledProps = new S3Properties();
            disabledProps.setEnabled(false);
            S3Exporter disabledExporter = new S3Exporter(s3Client, disabledProps, objectMapper);

            assertThat(disabledExporter.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("export() Tests")
    class ExportTests {

        @Test
        @DisplayName("Should export event to S3")
        void shouldExportEventToS3() throws Exception {
            when(s3Client.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
                .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

            Event event = createEvent();

            ExportResult result = exporter.export(event).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(1);
            verify(s3Client).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
        }

        @Test
        @DisplayName("Should use correct bucket")
        void shouldUseCorrectBucket() throws Exception {
            when(s3Client.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
                .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

            Event event = createEvent();
            exporter.export(event).get();

            ArgumentCaptor<PutObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(requestCaptor.capture(), any(AsyncRequestBody.class));

            assertThat(requestCaptor.getValue().bucket()).isEqualTo("test-bucket");
        }

        @Test
        @DisplayName("Should use NDJSON content type")
        void shouldUseNdjsonContentType() throws Exception {
            when(s3Client.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
                .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

            Event event = createEvent();
            exporter.export(event).get();

            ArgumentCaptor<PutObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(requestCaptor.capture(), any(AsyncRequestBody.class));

            assertThat(requestCaptor.getValue().contentType())
                .isEqualTo("application/x-ndjson");
        }

        @Test
        @DisplayName("Should return failure on S3 error")
        void shouldReturnFailureOnS3Error() throws Exception {
            CompletableFuture<PutObjectResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("S3 error"));

            when(s3Client.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
                .thenReturn(failedFuture);

            Event event = createEvent();

            ExportResult result = exporter.export(event).get();

            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("S3 error");
        }
    }

    @Nested
    @DisplayName("exportBatch() Tests")
    class ExportBatchTests {

        @Test
        @DisplayName("Should export batch successfully")
        void shouldExportBatchSuccessfully() throws Exception {
            when(s3Client.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
                .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

            List<Event> events = List.of(
                createEvent(),
                createEvent(),
                createEvent()
            );

            ExportResult result = exporter.exportBatch(events).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should create path with date hierarchy")
        void shouldCreatePathWithDateHierarchy() throws Exception {
            when(s3Client.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
                .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

            Event event = new Event(
                UUID.randomUUID(),
                Instant.parse("2024-06-15T14:30:00Z"),
                new Actor("user-1", Actor.ActorType.USER, "User", null, null, null),
                new Action(Action.ActionType.READ, "action", "security"),
                new Resource("resource", Resource.ResourceType.DOCUMENT, "Resource", null, null),
                null,
                null,
                null,
                null
            );

            exporter.exportBatch(List.of(event)).get();

            ArgumentCaptor<PutObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(requestCaptor.capture(), any(AsyncRequestBody.class));

            String key = requestCaptor.getValue().key();
            assertThat(key).contains("audit-events");
            assertThat(key).contains("2024/06/15/14");
            assertThat(key).contains("user-1");
            assertThat(key).endsWith(".ndjson");
        }

        @Test
        @DisplayName("Should handle empty list")
        void shouldHandleEmptyList() throws Exception {
            ExportResult result = exporter.exportBatch(List.of()).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isZero();
            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
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
