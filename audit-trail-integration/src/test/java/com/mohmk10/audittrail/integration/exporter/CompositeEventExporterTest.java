package com.mohmk10.audittrail.integration.exporter;

import com.mohmk10.audittrail.core.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompositeEventExporter Tests")
class CompositeEventExporterTest {

    @Mock
    private EventExporter splunkExporter;

    @Mock
    private EventExporter elkExporter;

    @Mock
    private EventExporter s3Exporter;

    @Nested
    @DisplayName("getName() Tests")
    class GetNameTests {

        @Test
        @DisplayName("Should return 'composite'")
        void shouldReturnComposite() {
            CompositeEventExporter exporter = new CompositeEventExporter(List.of());
            assertThat(exporter.getName()).isEqualTo("composite");
        }
    }

    @Nested
    @DisplayName("isEnabled() Tests")
    class IsEnabledTests {

        @Test
        @DisplayName("Should return true when at least one exporter is enabled")
        void shouldReturnTrueWhenOneEnabled() {
            when(splunkExporter.isEnabled()).thenReturn(true);
            when(elkExporter.isEnabled()).thenReturn(false);

            CompositeEventExporter exporter = new CompositeEventExporter(
                List.of(splunkExporter, elkExporter)
            );

            assertThat(exporter.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should return false when all exporters are disabled")
        void shouldReturnFalseWhenAllDisabled() {
            when(splunkExporter.isEnabled()).thenReturn(false);
            when(elkExporter.isEnabled()).thenReturn(false);

            CompositeEventExporter exporter = new CompositeEventExporter(
                List.of(splunkExporter, elkExporter)
            );

            assertThat(exporter.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should return false when no exporters")
        void shouldReturnFalseWhenNoExporters() {
            CompositeEventExporter exporter = new CompositeEventExporter(List.of());
            assertThat(exporter.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("export() Tests")
    class ExportTests {

        @Test
        @DisplayName("Should export to all enabled exporters")
        void shouldExportToAllEnabledExporters() throws Exception {
            when(splunkExporter.isEnabled()).thenReturn(true);
            when(splunkExporter.getName()).thenReturn("splunk");
            when(splunkExporter.export(any())).thenReturn(
                CompletableFuture.completedFuture(ExportResult.success("splunk", 1))
            );

            when(elkExporter.isEnabled()).thenReturn(true);
            when(elkExporter.getName()).thenReturn("elk");
            when(elkExporter.export(any())).thenReturn(
                CompletableFuture.completedFuture(ExportResult.success("elk", 1))
            );

            when(s3Exporter.isEnabled()).thenReturn(false);

            CompositeEventExporter exporter = new CompositeEventExporter(
                List.of(splunkExporter, elkExporter, s3Exporter)
            );

            Event event = createEvent();
            ExportResult result = exporter.export(event).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(2);

            verify(splunkExporter).export(event);
            verify(elkExporter).export(event);
            verify(s3Exporter, never()).export(any());
        }

        @Test
        @DisplayName("Should return failure when any exporter fails")
        void shouldReturnFailureWhenAnyExporterFails() throws Exception {
            when(splunkExporter.isEnabled()).thenReturn(true);
            when(splunkExporter.getName()).thenReturn("splunk");
            when(splunkExporter.export(any())).thenReturn(
                CompletableFuture.completedFuture(ExportResult.success("splunk", 1))
            );

            when(elkExporter.isEnabled()).thenReturn(true);
            when(elkExporter.getName()).thenReturn("elk");
            when(elkExporter.export(any())).thenReturn(
                CompletableFuture.completedFuture(ExportResult.failure("elk", "Connection failed"))
            );

            CompositeEventExporter exporter = new CompositeEventExporter(
                List.of(splunkExporter, elkExporter)
            );

            ExportResult result = exporter.export(createEvent()).get();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should return success when no exporters enabled")
        void shouldReturnSuccessWhenNoExportersEnabled() throws Exception {
            when(splunkExporter.isEnabled()).thenReturn(false);
            when(elkExporter.isEnabled()).thenReturn(false);

            CompositeEventExporter exporter = new CompositeEventExporter(
                List.of(splunkExporter, elkExporter)
            );

            ExportResult result = exporter.export(createEvent()).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isZero();
            assertThat(result.message()).contains("No enabled exporters");
        }
    }

    @Nested
    @DisplayName("exportBatch() Tests")
    class ExportBatchTests {

        @Test
        @DisplayName("Should export batch to all enabled exporters")
        void shouldExportBatchToAllEnabledExporters() throws Exception {
            when(splunkExporter.isEnabled()).thenReturn(true);
            when(splunkExporter.getName()).thenReturn("splunk");
            when(splunkExporter.exportBatch(any())).thenReturn(
                CompletableFuture.completedFuture(ExportResult.success("splunk", 3))
            );

            when(elkExporter.isEnabled()).thenReturn(true);
            when(elkExporter.getName()).thenReturn("elk");
            when(elkExporter.exportBatch(any())).thenReturn(
                CompletableFuture.completedFuture(ExportResult.success("elk", 3))
            );

            CompositeEventExporter exporter = new CompositeEventExporter(
                List.of(splunkExporter, elkExporter)
            );

            List<Event> events = List.of(
                createEvent(),
                createEvent(),
                createEvent()
            );

            ExportResult result = exporter.exportBatch(events).get();

            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(6);
        }

        @Test
        @DisplayName("Should aggregate messages from all exporters")
        void shouldAggregateMessagesFromAllExporters() throws Exception {
            when(splunkExporter.isEnabled()).thenReturn(true);
            when(splunkExporter.getName()).thenReturn("splunk");
            when(splunkExporter.exportBatch(any())).thenReturn(
                CompletableFuture.completedFuture(ExportResult.success("splunk", 1, "Sent to Splunk"))
            );

            when(elkExporter.isEnabled()).thenReturn(true);
            when(elkExporter.getName()).thenReturn("elk");
            when(elkExporter.exportBatch(any())).thenReturn(
                CompletableFuture.completedFuture(ExportResult.success("elk", 1, "Sent to ELK"))
            );

            CompositeEventExporter exporter = new CompositeEventExporter(
                List.of(splunkExporter, elkExporter)
            );

            ExportResult result = exporter.exportBatch(List.of(createEvent())).get();

            assertThat(result.message()).contains("splunk");
            assertThat(result.message()).contains("elk");
        }
    }

    @Nested
    @DisplayName("getExporters() Tests")
    class GetExportersTests {

        @Test
        @DisplayName("Should return all exporters")
        void shouldReturnAllExporters() {
            CompositeEventExporter exporter = new CompositeEventExporter(
                List.of(splunkExporter, elkExporter, s3Exporter)
            );

            assertThat(exporter.getExporters()).hasSize(3);
            assertThat(exporter.getExporters())
                .containsExactly(splunkExporter, elkExporter, s3Exporter);
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
