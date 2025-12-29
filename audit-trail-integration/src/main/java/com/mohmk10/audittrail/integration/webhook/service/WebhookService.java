package com.mohmk10.audittrail.integration.webhook.service;

import com.mohmk10.audittrail.integration.webhook.domain.Webhook;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookService {
    Webhook create(Webhook webhook);
    Webhook update(UUID id, Webhook webhook);
    void delete(UUID id);
    Optional<Webhook> findById(UUID id);
    List<Webhook> findByTenantId(String tenantId);
    List<Webhook> findActiveByTenantId(String tenantId);
    Webhook activate(UUID id);
    Webhook deactivate(UUID id);
    Webhook suspend(UUID id);
    String generateSecret();
    boolean verifySignature(String payload, String signature, String secret);
}
