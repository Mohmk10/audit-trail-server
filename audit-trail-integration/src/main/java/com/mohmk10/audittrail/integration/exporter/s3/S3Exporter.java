package com.mohmk10.audittrail.integration.exporter.s3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.integration.exporter.AbstractEventExporter;
import com.mohmk10.audittrail.integration.exporter.ExportResult;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class S3Exporter extends AbstractEventExporter {

    private static final String NAME = "s3";
    private static final DateTimeFormatter PATH_DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy/MM/dd/HH");

    private final S3AsyncClient s3Client;
    private final ObjectMapper objectMapper;
    private final S3Properties properties;
    private final boolean enabled;

    public S3Exporter(S3AsyncClient s3Client, S3Properties properties, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.enabled = properties.isEnabled();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    protected CompletableFuture<ExportResult> doExport(Event event) {
        return doExportBatch(List.of(event));
    }

    @Override
    protected CompletableFuture<ExportResult> doExportBatch(List<Event> events) {
        try {
            String content = serializeEvents(events);
            String key = buildKey(events);

            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .contentType("application/x-ndjson")
                .build();

            return s3Client.putObject(request, AsyncRequestBody.fromString(content))
                .thenApply(response -> {
                    log.debug("S3 export successful: bucket={}, key={}, events={}",
                        properties.getBucket(), key, events.size());
                    return ExportResult.success(NAME, events.size(),
                        "Exported to s3://" + properties.getBucket() + "/" + key);
                })
                .exceptionally(ex -> {
                    log.error("S3 export failed: {}", ex.getMessage());
                    return ExportResult.failure(NAME, ex.getMessage());
                });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize events for S3: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                ExportResult.failure(NAME, "Serialization error: " + e.getMessage())
            );
        }
    }

    private String serializeEvents(List<Event> events) throws JsonProcessingException {
        StringBuilder sb = new StringBuilder();
        for (Event event : events) {
            sb.append(objectMapper.writeValueAsString(event)).append("\n");
        }
        return sb.toString();
    }

    private String buildKey(List<Event> events) {
        Instant timestamp = events.isEmpty() ? Instant.now() : events.get(0).timestamp();
        String datePath = timestamp.atZone(ZoneOffset.UTC).format(PATH_DATE_FORMAT);

        String actorId = events.isEmpty() ? "unknown" :
            events.stream()
                .map(e -> e.actor().id())
                .distinct()
                .collect(Collectors.joining("-"));

        String filename = String.format("%s_%d.ndjson",
            timestamp.toEpochMilli(),
            events.size());

        return String.format("%s/%s/%s/%s",
            properties.getPrefix(),
            actorId,
            datePath,
            filename);
    }
}
