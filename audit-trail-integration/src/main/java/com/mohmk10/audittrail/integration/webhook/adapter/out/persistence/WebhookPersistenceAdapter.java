package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.DeliveryStatus;
import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookDelivery;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WebhookPersistenceAdapter {

    private final JpaWebhookRepository webhookRepository;
    private final JpaWebhookDeliveryRepository deliveryRepository;
    private final WebhookMapper webhookMapper;
    private final WebhookDeliveryMapper deliveryMapper;

    public WebhookPersistenceAdapter(
            JpaWebhookRepository webhookRepository,
            JpaWebhookDeliveryRepository deliveryRepository,
            WebhookMapper webhookMapper,
            WebhookDeliveryMapper deliveryMapper) {
        this.webhookRepository = webhookRepository;
        this.deliveryRepository = deliveryRepository;
        this.webhookMapper = webhookMapper;
        this.deliveryMapper = deliveryMapper;
    }

    // Webhook operations
    public Webhook saveWebhook(Webhook webhook) {
        WebhookEntity entity = webhookMapper.toEntity(webhook);
        WebhookEntity saved = webhookRepository.save(entity);
        return webhookMapper.toDomain(saved);
    }

    public Optional<Webhook> findWebhookById(UUID id) {
        return webhookRepository.findById(id)
            .map(webhookMapper::toDomain);
    }

    public List<Webhook> findWebhooksByTenantId(String tenantId) {
        return webhookRepository.findByTenantId(tenantId)
            .stream()
            .map(webhookMapper::toDomain)
            .toList();
    }

    public List<Webhook> findWebhooksByTenantIdAndStatus(String tenantId, WebhookStatus status) {
        return webhookRepository.findByTenantIdAndStatus(tenantId, status)
            .stream()
            .map(webhookMapper::toDomain)
            .toList();
    }

    public List<Webhook> findWebhooksByStatus(WebhookStatus status) {
        return webhookRepository.findByStatus(status)
            .stream()
            .map(webhookMapper::toDomain)
            .toList();
    }

    public void deleteWebhook(UUID id) {
        webhookRepository.deleteById(id);
    }

    public boolean webhookExists(UUID id) {
        return webhookRepository.existsById(id);
    }

    // Delivery operations
    public WebhookDelivery saveDelivery(WebhookDelivery delivery) {
        WebhookDeliveryEntity entity = deliveryMapper.toEntity(delivery);
        WebhookDeliveryEntity saved = deliveryRepository.save(entity);
        return deliveryMapper.toDomain(saved);
    }

    public Optional<WebhookDelivery> findDeliveryById(UUID id) {
        return deliveryRepository.findById(id)
            .map(deliveryMapper::toDomain);
    }

    public List<WebhookDelivery> findDeliveriesByWebhookId(UUID webhookId) {
        return deliveryRepository.findByWebhookIdOrderByCreatedAtDesc(webhookId)
            .stream()
            .map(deliveryMapper::toDomain)
            .toList();
    }

    public List<WebhookDelivery> findDeliveriesByStatus(DeliveryStatus status) {
        return deliveryRepository.findByStatus(status)
            .stream()
            .map(deliveryMapper::toDomain)
            .toList();
    }

    public List<WebhookDelivery> findDeliveriesReadyForRetry(Instant before) {
        return deliveryRepository.findByStatusAndNextRetryAtBefore(DeliveryStatus.RETRYING, before)
            .stream()
            .map(deliveryMapper::toDomain)
            .toList();
    }

    public long countDeliveriesByWebhookIdAndStatus(UUID webhookId, DeliveryStatus status) {
        return deliveryRepository.countByWebhookIdAndStatus(webhookId, status);
    }
}
