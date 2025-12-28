package com.mohmk10.audittrail.search.adapter.in.rest.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.mohmk10.audittrail.core.domain.Event;

public record EventSearchResponse(
        UUID id,
        Instant timestamp,
        ActorResponse actor,
        ActionResponse action,
        ResourceResponse resource,
        MetadataResponse metadata,
        String hash
) {
    public record ActorResponse(String id, String type, String name, String ip) {}
    public record ActionResponse(String type, String description, String category) {}
    public record ResourceResponse(String id, String type, String name) {}
    public record MetadataResponse(String source, String tenantId, String correlationId, String sessionId, Map<String, String> tags) {}

    public static EventSearchResponse from(Event event) {
        return new EventSearchResponse(
                event.id(),
                event.timestamp(),
                event.actor() != null ? new ActorResponse(
                        event.actor().id(),
                        event.actor().type() != null ? event.actor().type().name() : null,
                        event.actor().name(),
                        event.actor().ip()
                ) : null,
                event.action() != null ? new ActionResponse(
                        event.action().type() != null ? event.action().type().name() : null,
                        event.action().description(),
                        event.action().category()
                ) : null,
                event.resource() != null ? new ResourceResponse(
                        event.resource().id(),
                        event.resource().type() != null ? event.resource().type().name() : null,
                        event.resource().name()
                ) : null,
                event.metadata() != null ? new MetadataResponse(
                        event.metadata().source(),
                        event.metadata().tenantId(),
                        event.metadata().correlationId(),
                        event.metadata().sessionId(),
                        event.metadata().tags()
                ) : null,
                event.hash()
        );
    }
}
