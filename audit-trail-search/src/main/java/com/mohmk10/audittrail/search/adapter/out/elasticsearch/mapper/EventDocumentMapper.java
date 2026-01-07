package com.mohmk10.audittrail.search.adapter.out.elasticsearch.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.document.EventDocument;

@Component
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class EventDocumentMapper {

    public EventDocument toDocument(Event event) {
        if (event == null) {
            return null;
        }

        EventDocument doc = new EventDocument();
        doc.setId(event.id().toString());
        doc.setTimestamp(event.timestamp());
        doc.setHash(event.hash());

        mapActor(event.actor(), doc);
        mapAction(event.action(), doc);
        mapResource(event.resource(), doc);
        mapMetadata(event.metadata(), doc);

        return doc;
    }

    public Event toDomain(EventDocument doc) {
        if (doc == null) {
            return null;
        }

        return new Event(
                java.util.UUID.fromString(doc.getId()),
                doc.getTimestamp(),
                new Actor(
                        doc.getActorId(),
                        doc.getActorType() != null ? Actor.ActorType.valueOf(doc.getActorType()) : null,
                        doc.getActorName(),
                        doc.getActorIp(),
                        null,
                        null
                ),
                new Action(
                        doc.getActionType() != null ? Action.ActionType.valueOf(doc.getActionType()) : null,
                        doc.getActionDescription(),
                        doc.getActionCategory()
                ),
                new Resource(
                        doc.getResourceId(),
                        doc.getResourceType() != null ? Resource.ResourceType.valueOf(doc.getResourceType()) : null,
                        doc.getResourceName(),
                        null,
                        null
                ),
                new EventMetadata(
                        doc.getSource(),
                        doc.getTenantId(),
                        doc.getCorrelationId(),
                        doc.getSessionId(),
                        tagsListToMap(doc.getTags()),
                        null
                ),
                null,
                doc.getHash(),
                null
        );
    }

    public List<Event> toDomainList(List<EventDocument> documents) {
        if (documents == null) {
            return new ArrayList<>();
        }
        return documents.stream().map(this::toDomain).toList();
    }

    public List<EventDocument> toDocumentList(List<Event> events) {
        if (events == null) {
            return new ArrayList<>();
        }
        return events.stream().map(this::toDocument).toList();
    }

    private void mapActor(Actor actor, EventDocument doc) {
        if (actor != null) {
            doc.setActorId(actor.id());
            doc.setActorType(actor.type() != null ? actor.type().name() : null);
            doc.setActorName(actor.name());
            doc.setActorIp(actor.ip());
        }
    }

    private void mapAction(Action action, EventDocument doc) {
        if (action != null) {
            doc.setActionType(action.type() != null ? action.type().name() : null);
            doc.setActionDescription(action.description());
            doc.setActionCategory(action.category());
        }
    }

    private void mapResource(Resource resource, EventDocument doc) {
        if (resource != null) {
            doc.setResourceId(resource.id());
            doc.setResourceType(resource.type() != null ? resource.type().name() : null);
            doc.setResourceName(resource.name());
        }
    }

    private void mapMetadata(EventMetadata metadata, EventDocument doc) {
        if (metadata != null) {
            doc.setSource(metadata.source());
            doc.setTenantId(metadata.tenantId());
            doc.setCorrelationId(metadata.correlationId());
            doc.setSessionId(metadata.sessionId());
            doc.setTags(tagsMapToList(metadata.tags()));
        }
    }

    private List<String> tagsMapToList(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        List<String> tagList = new ArrayList<>();
        tags.forEach((k, v) -> tagList.add(k + ":" + v));
        return tagList;
    }

    private Map<String, String> tagsListToMap(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        java.util.Map<String, String> tagMap = new java.util.HashMap<>();
        for (String tag : tags) {
            String[] parts = tag.split(":", 2);
            if (parts.length == 2) {
                tagMap.put(parts[0], parts[1]);
            }
        }
        return tagMap;
    }
}
