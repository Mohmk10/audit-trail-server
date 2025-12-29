package com.mohmk10.audittrail.integration.webhook.adapter.out.persistence;

import com.mohmk10.audittrail.integration.webhook.domain.WebhookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaWebhookRepository extends JpaRepository<WebhookEntity, UUID> {
    List<WebhookEntity> findByTenantId(String tenantId);
    List<WebhookEntity> findByTenantIdAndStatus(String tenantId, WebhookStatus status);
    List<WebhookEntity> findByStatus(WebhookStatus status);
}
