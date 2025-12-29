package com.mohmk10.audittrail.integration.exporter;

import com.mohmk10.audittrail.core.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractEventExporter implements EventExporter {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public CompletableFuture<ExportResult> export(Event event) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(
                ExportResult.failure(getName(), "Exporter is disabled")
            );
        }

        return doExport(event);
    }

    @Override
    public CompletableFuture<ExportResult> exportBatch(List<Event> events) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(
                ExportResult.failure(getName(), "Exporter is disabled")
            );
        }

        if (events == null || events.isEmpty()) {
            return CompletableFuture.completedFuture(
                ExportResult.success(getName(), 0, "No events to export")
            );
        }

        return doExportBatch(events);
    }

    protected abstract CompletableFuture<ExportResult> doExport(Event event);

    protected abstract CompletableFuture<ExportResult> doExportBatch(List<Event> events);
}
