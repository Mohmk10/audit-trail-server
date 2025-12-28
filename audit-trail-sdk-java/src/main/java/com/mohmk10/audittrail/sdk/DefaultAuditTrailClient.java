package com.mohmk10.audittrail.sdk;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.sdk.config.AuditTrailConfig;
import com.mohmk10.audittrail.sdk.exception.AuditTrailApiException;
import com.mohmk10.audittrail.sdk.exception.AuditTrailException;
import com.mohmk10.audittrail.sdk.http.HttpClientWrapper;
import com.mohmk10.audittrail.sdk.model.BatchEventResponse;
import com.mohmk10.audittrail.sdk.model.Event;
import com.mohmk10.audittrail.sdk.model.EventResponse;

public class DefaultAuditTrailClient implements AuditTrailClient {
    private static final Logger log = LoggerFactory.getLogger(DefaultAuditTrailClient.class);
    private static final String EVENTS_PATH = "/api/v1/events";
    private static final String BATCH_PATH = "/api/v1/events/batch";

    private final HttpClientWrapper httpClient;
    private final ObjectMapper objectMapper;

    public DefaultAuditTrailClient(AuditTrailConfig config) {
        this.httpClient = new HttpClientWrapper(config);
        this.objectMapper = createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Override
    public EventResponse log(Event event) {
        try {
            String requestBody = objectMapper.writeValueAsString(toRequestMap(event));
            String responseBody = httpClient.post(EVENTS_PATH, requestBody);
            return objectMapper.readValue(responseBody, EventResponse.class);
        } catch (JsonProcessingException e) {
            throw new AuditTrailException("Failed to serialize/deserialize event", e);
        }
    }

    @Override
    public List<EventResponse> logBatch(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            Map<String, Object> batchRequest = new HashMap<>();
            batchRequest.put("events", events.stream()
                    .map(this::toRequestMap)
                    .collect(Collectors.toList()));

            String requestBody = objectMapper.writeValueAsString(batchRequest);
            String responseBody = httpClient.post(BATCH_PATH, requestBody);

            BatchEventResponse batchResponse = objectMapper.readValue(responseBody, BatchEventResponse.class);
            return batchResponse.getEvents() != null ? batchResponse.getEvents() : Collections.emptyList();
        } catch (JsonProcessingException e) {
            throw new AuditTrailException("Failed to serialize/deserialize batch events", e);
        }
    }

    @Override
    public Optional<EventResponse> findById(UUID id) {
        try {
            String responseBody = httpClient.get(EVENTS_PATH + "/" + id);
            EventResponse response = objectMapper.readValue(responseBody, EventResponse.class);
            return Optional.of(response);
        } catch (AuditTrailApiException e) {
            if (e.getStatusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        } catch (JsonProcessingException e) {
            throw new AuditTrailException("Failed to deserialize event response", e);
        }
    }

    @Override
    public CompletableFuture<EventResponse> logAsync(Event event) {
        return CompletableFuture.supplyAsync(() -> log(event));
    }

    @Override
    public CompletableFuture<List<EventResponse>> logBatchAsync(List<Event> events) {
        return CompletableFuture.supplyAsync(() -> logBatch(events));
    }

    @Override
    public void close() {
        log.debug("Closing AuditTrailClient");
    }

    private Map<String, Object> toRequestMap(Event event) {
        Map<String, Object> map = new HashMap<>();

        if (event.getActor() != null) {
            Map<String, Object> actor = new HashMap<>();
            actor.put("id", event.getActor().getId());
            actor.put("type", event.getActor().getType());
            if (event.getActor().getName() != null) {
                actor.put("name", event.getActor().getName());
            }
            if (event.getActor().getIp() != null) {
                actor.put("ip", event.getActor().getIp());
            }
            if (event.getActor().getUserAgent() != null) {
                actor.put("userAgent", event.getActor().getUserAgent());
            }
            if (event.getActor().getAttributes() != null && !event.getActor().getAttributes().isEmpty()) {
                actor.put("attributes", event.getActor().getAttributes());
            }
            map.put("actor", actor);
        }

        if (event.getAction() != null) {
            Map<String, Object> action = new HashMap<>();
            action.put("type", event.getAction().getType());
            if (event.getAction().getDescription() != null) {
                action.put("description", event.getAction().getDescription());
            }
            if (event.getAction().getCategory() != null) {
                action.put("category", event.getAction().getCategory());
            }
            map.put("action", action);
        }

        if (event.getResource() != null) {
            Map<String, Object> resource = new HashMap<>();
            resource.put("id", event.getResource().getId());
            resource.put("type", event.getResource().getType());
            if (event.getResource().getName() != null) {
                resource.put("name", event.getResource().getName());
            }
            if (event.getResource().getBefore() != null) {
                resource.put("before", event.getResource().getBefore());
            }
            if (event.getResource().getAfter() != null) {
                resource.put("after", event.getResource().getAfter());
            }
            map.put("resource", resource);
        }

        if (event.getMetadata() != null) {
            Map<String, Object> metadata = new HashMap<>();
            if (event.getMetadata().getSource() != null) {
                metadata.put("source", event.getMetadata().getSource());
            }
            if (event.getMetadata().getTenantId() != null) {
                metadata.put("tenantId", event.getMetadata().getTenantId());
            }
            if (event.getMetadata().getCorrelationId() != null) {
                metadata.put("correlationId", event.getMetadata().getCorrelationId());
            }
            if (event.getMetadata().getSessionId() != null) {
                metadata.put("sessionId", event.getMetadata().getSessionId());
            }
            if (event.getMetadata().getTags() != null && !event.getMetadata().getTags().isEmpty()) {
                metadata.put("tags", event.getMetadata().getTags());
            }
            if (event.getMetadata().getExtra() != null && !event.getMetadata().getExtra().isEmpty()) {
                metadata.put("extra", event.getMetadata().getExtra());
            }
            if (!metadata.isEmpty()) {
                map.put("metadata", metadata);
            }
        }

        return map;
    }
}
