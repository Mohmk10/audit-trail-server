package com.mohmk10.audittrail.ingestion.service;

import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EventEnrichmentServiceImpl implements EventEnrichmentService {

    @Override
    public Event enrich(Event event) {
        UUID id = event.id() != null ? event.id() : UUID.randomUUID();
        Instant timestamp = event.timestamp() != null ? event.timestamp() : Instant.now();

        Actor enrichedActor = enrichActor(event.actor());
        EventMetadata enrichedMetadata = enrichMetadata(event.metadata());

        return new Event(
                id,
                timestamp,
                enrichedActor,
                event.action(),
                event.resource(),
                enrichedMetadata,
                event.previousHash(),
                event.hash(),
                event.signature()
        );
    }

    private Actor enrichActor(Actor actor) {
        if (actor == null || actor.ip() == null) {
            return actor;
        }

        Map<String, String> attributes = new HashMap<>();
        if (actor.attributes() != null) {
            attributes.putAll(actor.attributes());
        }

        String geoLocation = resolveGeoLocation(actor.ip());
        if (geoLocation != null) {
            attributes.put("geoLocation", geoLocation);
        }

        String parsedUserAgent = parseUserAgent(actor.userAgent());
        if (parsedUserAgent != null) {
            attributes.put("browserInfo", parsedUserAgent);
        }

        return new Actor(
                actor.id(),
                actor.type(),
                actor.name(),
                actor.ip(),
                actor.userAgent(),
                attributes.isEmpty() ? actor.attributes() : attributes
        );
    }

    private EventMetadata enrichMetadata(EventMetadata metadata) {
        if (metadata == null) {
            return new EventMetadata(
                    "api",
                    "default",
                    UUID.randomUUID().toString(),
                    null,
                    null,
                    null
            );
        }

        String correlationId = metadata.correlationId() != null
                ? metadata.correlationId()
                : UUID.randomUUID().toString();

        return new EventMetadata(
                metadata.source(),
                metadata.tenantId(),
                correlationId,
                metadata.sessionId(),
                metadata.tags(),
                metadata.extra()
        );
    }

    private String resolveGeoLocation(String ip) {
        if (ip == null || ip.equals("127.0.0.1") || ip.equals("localhost")) {
            return "Local";
        }
        return "Unknown";
    }

    private String parseUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null;
        }

        if (userAgent.contains("Chrome")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Safari")) {
            return "Safari";
        } else if (userAgent.contains("Edge")) {
            return "Edge";
        }
        return "Other";
    }
}
