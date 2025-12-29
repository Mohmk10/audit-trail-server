package com.mohmk10.audittrail.integration.exporter.elk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.integration.exporter.AbstractEventExporter;
import com.mohmk10.audittrail.integration.exporter.ExportResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ElkExporter extends AbstractEventExporter {

    private static final String NAME = "elk";
    private static final DateTimeFormatter INDEX_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ElkProperties properties;
    private final boolean enabled;

    public ElkExporter(ElkProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.enabled = properties.isEnabled();

        WebClient.Builder builder = WebClient.builder()
            .baseUrl(properties.getUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (properties.getUsername() != null && properties.getPassword() != null) {
            builder.defaultHeaders(headers ->
                headers.setBasicAuth(properties.getUsername(), properties.getPassword())
            );
        }

        if (properties.getApiKey() != null) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "ApiKey " + properties.getApiKey());
        }

        this.webClient = builder.build();
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
        try {
            String indexName = buildIndexName(event);
            String document = objectMapper.writeValueAsString(eventToDocument(event));

            return webClient.post()
                .uri("/{index}/_doc/{id}", indexName, event.id().toString())
                .bodyValue(document)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .map(response -> {
                    log.debug("ELK export successful: eventId={}", event.id());
                    return ExportResult.success(NAME, 1);
                })
                .onErrorResume(ex -> {
                    log.error("ELK export failed: {}", ex.getMessage());
                    return Mono.just(ExportResult.failure(NAME, ex.getMessage()));
                })
                .toFuture();

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for ELK: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                ExportResult.failure(NAME, "Serialization error: " + e.getMessage())
            );
        }
    }

    @Override
    protected CompletableFuture<ExportResult> doExportBatch(List<Event> events) {
        try {
            String bulkPayload = buildBulkPayload(events);

            return webClient.post()
                .uri("/_bulk")
                .bodyValue(bulkPayload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .map(response -> {
                    log.debug("ELK bulk export successful: {} events", events.size());
                    return ExportResult.success(NAME, events.size());
                })
                .onErrorResume(ex -> {
                    log.error("ELK bulk export failed: {}", ex.getMessage());
                    return Mono.just(ExportResult.failure(NAME, ex.getMessage()));
                })
                .toFuture();

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize events for ELK bulk: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                ExportResult.failure(NAME, "Serialization error: " + e.getMessage())
            );
        }
    }

    private String buildIndexName(Event event) {
        String dateStr = event.timestamp().atZone(java.time.ZoneOffset.UTC)
            .format(INDEX_DATE_FORMAT);
        return properties.getIndexPrefix() + "-" + dateStr;
    }

    private String buildBulkPayload(List<Event> events) throws JsonProcessingException {
        StringBuilder sb = new StringBuilder();
        for (Event event : events) {
            String indexName = buildIndexName(event);

            // Index action line
            Map<String, Object> action = new HashMap<>();
            Map<String, Object> indexMeta = new HashMap<>();
            indexMeta.put("_index", indexName);
            indexMeta.put("_id", event.id().toString());
            action.put("index", indexMeta);
            sb.append(objectMapper.writeValueAsString(action)).append("\n");

            // Document line
            sb.append(objectMapper.writeValueAsString(eventToDocument(event))).append("\n");
        }
        return sb.toString();
    }

    private Map<String, Object> eventToDocument(Event event) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", event.id().toString());
        doc.put("actor_id", event.actor().id());
        doc.put("actor_type", event.actor().type());
        doc.put("action_type", event.action().type());
        doc.put("action_description", event.action().description());
        doc.put("resource_type", event.resource().type());
        doc.put("resource_id", event.resource().id());
        doc.put("@timestamp", event.timestamp().toString());
        if (event.metadata() != null) {
            doc.put("metadata", event.metadata());
        }
        return doc;
    }
}
