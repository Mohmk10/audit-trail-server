package com.mohmk10.audittrail.integration.exporter;

import java.time.Instant;

public record ExportResult(
    String exporterName,
    boolean success,
    int eventsProcessed,
    String message,
    Instant timestamp
) {
    public static ExportResult success(String exporterName, int eventsProcessed) {
        return new ExportResult(
            exporterName,
            true,
            eventsProcessed,
            "Export successful",
            Instant.now()
        );
    }

    public static ExportResult success(String exporterName, int eventsProcessed, String message) {
        return new ExportResult(
            exporterName,
            true,
            eventsProcessed,
            message,
            Instant.now()
        );
    }

    public static ExportResult failure(String exporterName, String errorMessage) {
        return new ExportResult(
            exporterName,
            false,
            0,
            errorMessage,
            Instant.now()
        );
    }

    public static ExportResult failure(String exporterName, int eventsProcessed, String errorMessage) {
        return new ExportResult(
            exporterName,
            false,
            eventsProcessed,
            errorMessage,
            Instant.now()
        );
    }
}
