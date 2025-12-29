package com.mohmk10.audittrail.integration.exporter;

import com.mohmk10.audittrail.core.domain.Event;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EventExporter {

    String getName();

    boolean isEnabled();

    CompletableFuture<ExportResult> export(Event event);

    CompletableFuture<ExportResult> exportBatch(List<Event> events);
}
