package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaWebhookDeliveryRepository extends JpaRepository<WebhookDeliveryEntity, UUID> {
    List<WebhookDeliveryEntity> findByWebhookIdOrderByCreatedAtDesc(UUID webhookId);
    List<WebhookDeliveryEntity> findByStatus(DeliveryStatus status);
    List<WebhookDeliveryEntity> findByStatusAndNextRetryAtBefore(DeliveryStatus status, Instant time);
    long countByWebhookIdAndStatus(UUID webhookId, DeliveryStatus status);
}
