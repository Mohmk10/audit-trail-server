package com.mohmk10.audittrail.storage.adapter.out.persistence.mapper;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;

public final class EventMapper {

    private EventMapper() {
    }

    public static EventEntity toEntity(Event domain) {
        if (domain == null) {
            return null;
        }

        EventEntity entity = new EventEntity();
        entity.setId(domain.id());
        entity.setTimestamp(domain.timestamp());

        if (domain.actor() != null) {
            entity.setActorId(domain.actor().id());
            entity.setActorType(domain.actor().type().name());
            entity.setActorName(domain.actor().name());
            entity.setActorIp(domain.actor().ip());
            entity.setActorUserAgent(domain.actor().userAgent());
            entity.setActorAttributes(domain.actor().attributes());
        }

        if (domain.action() != null) {
            entity.setActionType(domain.action().type().name());
            entity.setActionDescription(domain.action().description());
            entity.setActionCategory(domain.action().category());
        }

        if (domain.resource() != null) {
            entity.setResourceId(domain.resource().id());
            entity.setResourceType(domain.resource().type().name());
            entity.setResourceName(domain.resource().name());
            entity.setResourceBefore(domain.resource().before());
            entity.setResourceAfter(domain.resource().after());
        }

        if (domain.metadata() != null) {
            entity.setMetadataSource(domain.metadata().source());
            entity.setTenantId(domain.metadata().tenantId());
            entity.setCorrelationId(domain.metadata().correlationId());
            entity.setSessionId(domain.metadata().sessionId());
            entity.setTags(domain.metadata().tags());
            entity.setExtra(domain.metadata().extra());
        }

        entity.setPreviousHash(domain.previousHash());
        entity.setHash(domain.hash());
        entity.setSignature(domain.signature());

        return entity;
    }

    public static Event toDomain(EventEntity entity) {
        if (entity == null) {
            return null;
        }

        Actor actor = new Actor(
                entity.getActorId(),
                Actor.ActorType.valueOf(entity.getActorType()),
                entity.getActorName(),
                entity.getActorIp(),
                entity.getActorUserAgent(),
                entity.getActorAttributes()
        );

        Action action = new Action(
                Action.ActionType.valueOf(entity.getActionType()),
                entity.getActionDescription(),
                entity.getActionCategory()
        );

        Resource resource = new Resource(
                entity.getResourceId(),
                Resource.ResourceType.valueOf(entity.getResourceType()),
                entity.getResourceName(),
                entity.getResourceBefore(),
                entity.getResourceAfter()
        );

        EventMetadata metadata = new EventMetadata(
                entity.getMetadataSource(),
                entity.getTenantId(),
                entity.getCorrelationId(),
                entity.getSessionId(),
                entity.getTags(),
                entity.getExtra()
        );

        return new Event(
                entity.getId(),
                entity.getTimestamp(),
                actor,
                action,
                resource,
                metadata,
                entity.getPreviousHash(),
                entity.getHash(),
                entity.getSignature()
        );
    }
}
