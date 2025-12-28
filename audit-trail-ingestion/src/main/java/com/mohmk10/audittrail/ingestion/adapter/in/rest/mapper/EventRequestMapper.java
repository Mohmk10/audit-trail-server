package com.mohmk10.audittrail.ingestion.adapter.in.rest.mapper;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ActionRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ActorRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventMetadataRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ResourceRequest;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;

@Component
public class EventRequestMapper {

    public Event toEvent(EventRequest request) {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                toActor(request.actor()),
                toAction(request.action()),
                toResource(request.resource()),
                toMetadata(request.metadata()),
                null,
                null,
                null
        );
    }

    public EventResponse toResponse(Event event) {
        return new EventResponse(
                event.id(),
                event.timestamp(),
                event.hash(),
                "STORED"
        );
    }

    private Actor toActor(ActorRequest request) {
        Actor.ActorType actorType;
        try {
            actorType = Actor.ActorType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            actorType = Actor.ActorType.USER;
        }

        return new Actor(
                request.id(),
                actorType,
                request.name() != null ? request.name() : request.id(),
                request.ip(),
                request.userAgent(),
                request.attributes()
        );
    }

    private Action toAction(ActionRequest request) {
        Action.ActionType actionType;
        try {
            actionType = Action.ActionType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            actionType = Action.ActionType.READ;
        }

        return new Action(
                actionType,
                request.description(),
                request.category()
        );
    }

    private Resource toResource(ResourceRequest request) {
        Resource.ResourceType resourceType;
        try {
            resourceType = Resource.ResourceType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            resourceType = Resource.ResourceType.DOCUMENT;
        }

        return new Resource(
                request.id(),
                resourceType,
                request.name() != null ? request.name() : request.id(),
                request.before(),
                request.after()
        );
    }

    private EventMetadata toMetadata(EventMetadataRequest request) {
        if (request == null) {
            return new EventMetadata(
                    "unknown",
                    "default",
                    null,
                    null,
                    null,
                    null
            );
        }

        return new EventMetadata(
                request.source(),
                request.tenantId(),
                request.correlationId(),
                request.sessionId(),
                request.tags(),
                request.extra()
        );
    }
}
