package com.mohmk10.audittrail.integration.exporter.splunk;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SplunkExporter extends AbstractEventExporter {

    private static final String NAME = "splunk";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final SplunkProperties properties;
    private final boolean enabled;

    public SplunkExporter(SplunkProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.enabled = properties.isEnabled();

        this.webClient = WebClient.builder()
            .baseUrl(properties.getUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Splunk " + properties.getToken())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
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
            String payload = buildPayload(events);

            return webClient.post()
                .uri("/services/collector/event")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .map(response -> {
                    log.debug("Splunk export successful: {} events", events.size());
                    return ExportResult.success(NAME, events.size());
                })
                .onErrorResume(ex -> {
                    log.error("Splunk export failed: {}", ex.getMessage());
                    return Mono.just(ExportResult.failure(NAME, ex.getMessage()));
                })
                .toFuture();

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize events for Splunk: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                ExportResult.failure(NAME, "Serialization error: " + e.getMessage())
            );
        }
    }

    private String buildPayload(List<Event> events) throws JsonProcessingException {
        StringBuilder sb = new StringBuilder();
        for (Event event : events) {
            Map<String, Object> splunkEvent = new HashMap<>();
            splunkEvent.put("time", event.timestamp().getEpochSecond());
            splunkEvent.put("sourcetype", properties.getSourceType());
            splunkEvent.put("source", event.resource().type());
            splunkEvent.put("index", properties.getIndex());
            splunkEvent.put("event", eventToMap(event));

            sb.append(objectMapper.writeValueAsString(splunkEvent));
        }
        return sb.toString();
    }

    private Map<String, Object> eventToMap(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", event.id().toString());
        map.put("actor_id", event.actor().id());
        map.put("actor_type", event.actor().type());
        map.put("action_type", event.action().type());
        map.put("action_description", event.action().description());
        map.put("resource_type", event.resource().type());
        map.put("resource_id", event.resource().id());
        map.put("timestamp", event.timestamp().toString());
        if (event.metadata() != null) {
            map.put("metadata", event.metadata());
        }
        return map;
    }
}
