package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;

@Component
public class WebhookMapper {
    
    public WebhookEntity toEntity(Webhook domain) {
        WebhookEntity entity = new WebhookEntity();
        entity.setId(domain.id());
        entity.setTenantId(domain.tenantId());
        entity.setName(domain.name());
        entity.setUrl(domain.url());
        entity.setSecret(domain.secret());
        entity.setEvents(domain.events() != null ? new HashSet<>(domain.events()) : new HashSet<>());
        entity.setStatus(domain.status());
        entity.setHeaders(domain.headers() != null ? new HashMap<>(domain.headers()) : new HashMap<>());
        entity.setMaxRetries(domain.maxRetries());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        return entity;
    }
    
    public Webhook toDomain(WebhookEntity entity) {
        return new Webhook(
            entity.getId(),
            entity.getTenantId(),
            entity.getName(),
            entity.getUrl(),
            entity.getSecret(),
            entity.getEvents() != null ? new HashSet<>(entity.getEvents()) : new HashSet<>(),
            entity.getStatus(),
            entity.getHeaders() != null ? new HashMap<>(entity.getHeaders()) : new HashMap<>(),
            entity.getMaxRetries(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
