package com.mohmk10.audittrail.integration.exporter;

import com.mohmk10.audittrail.core.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CompositeEventExporter implements EventExporter {

    private static final Logger log = LoggerFactory.getLogger(CompositeEventExporter.class);
    private static final String NAME = "composite";

    private final List<EventExporter> exporters;

    public CompositeEventExporter(List<EventExporter> exporters) {
        this.exporters = exporters;
        log.info("Composite exporter initialized with {} exporters: {}",
            exporters.size(),
            exporters.stream()
                .filter(EventExporter::isEnabled)
                .map(EventExporter::getName)
                .collect(Collectors.joining(", ")));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled() {
        return exporters.stream().anyMatch(EventExporter::isEnabled);
    }

    @Override
    public CompletableFuture<ExportResult> export(Event event) {
        List<CompletableFuture<ExportResult>> futures = exporters.stream()
            .filter(EventExporter::isEnabled)
            .map(exporter -> exporter.export(event))
            .toList();

        return aggregateResults(futures);
    }

    @Override
    public CompletableFuture<ExportResult> exportBatch(List<Event> events) {
        List<CompletableFuture<ExportResult>> futures = exporters.stream()
            .filter(EventExporter::isEnabled)
            .map(exporter -> exporter.exportBatch(events))
            .toList();

        return aggregateResults(futures);
    }

    private CompletableFuture<ExportResult> aggregateResults(
            List<CompletableFuture<ExportResult>> futures) {

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(
                ExportResult.success(NAME, 0, "No enabled exporters")
            );
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<ExportResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

                int totalProcessed = results.stream()
                    .mapToInt(ExportResult::eventsProcessed)
                    .sum();

                boolean allSuccess = results.stream()
                    .allMatch(ExportResult::success);

                String message = results.stream()
                    .map(r -> r.exporterName() + ": " + r.message())
                    .collect(Collectors.joining("; "));

                if (allSuccess) {
                    return ExportResult.success(NAME, totalProcessed, message);
                } else {
                    return ExportResult.failure(NAME, totalProcessed, message);
                }
            });
    }

    public List<EventExporter> getExporters() {
        return exporters;
    }
}
