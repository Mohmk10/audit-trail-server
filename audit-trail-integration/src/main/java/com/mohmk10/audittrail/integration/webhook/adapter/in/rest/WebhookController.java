package com.mohmk10.audittrail.integration.webhook.adapter.in.rest;

import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookDelivery;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookEvent;
import com.mohmk10.audittrail.integration.webhook.service.WebhookDeliveryService;
import com.mohmk10.audittrail.integration.webhook.service.WebhookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final WebhookService webhookService;
    private final WebhookDeliveryService deliveryService;

    public WebhookController(WebhookService webhookService, WebhookDeliveryService deliveryService) {
        this.webhookService = webhookService;
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public ResponseEntity<WebhookResponse> createWebhook(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody WebhookRequest request) {

        Webhook webhook = Webhook.builder()
            .tenantId(tenantId)
            .name(request.name())
            .url(request.url())
            .events(request.events())
            .headers(request.headers())
            .maxRetries(request.maxRetries() != null ? request.maxRetries() : Webhook.DEFAULT_MAX_RETRIES)
            .build();

        Webhook created = webhookService.create(webhook);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(WebhookResponse.from(created));
    }

    @GetMapping
    public ResponseEntity<List<WebhookResponse>> listWebhooks(
            @RequestHeader("X-Tenant-ID") String tenantId) {

        List<WebhookResponse> webhooks = webhookService.findByTenantId(tenantId)
            .stream()
            .map(WebhookResponse::from)
            .toList();

        return ResponseEntity.ok(webhooks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WebhookResponse> getWebhook(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id) {

        return webhookService.findById(id)
            .filter(w -> w.tenantId().equals(tenantId))
            .map(webhook -> ResponseEntity.ok(WebhookResponse.from(webhook)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<WebhookResponse> updateWebhook(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody WebhookRequest request) {

        return webhookService.findById(id)
            .filter(w -> w.tenantId().equals(tenantId))
            .map(existing -> {
                Webhook updateData = Webhook.builder()
                    .name(request.name())
                    .url(request.url())
                    .events(request.events())
                    .headers(request.headers())
                    .maxRetries(request.maxRetries() != null ? request.maxRetries() : 0)
                    .build();
                Webhook updated = webhookService.update(id, updateData);
                return ResponseEntity.ok(WebhookResponse.from(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhook(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id) {

        return webhookService.findById(id)
            .filter(w -> w.tenantId().equals(tenantId))
            .map(webhook -> {
                webhookService.delete(id);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<WebhookResponse> activateWebhook(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id) {

        return webhookService.findById(id)
            .filter(w -> w.tenantId().equals(tenantId))
            .map(webhook -> {
                Webhook activated = webhookService.activate(id);
                return ResponseEntity.ok(WebhookResponse.from(activated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<WebhookResponse> deactivateWebhook(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id) {

        return webhookService.findById(id)
            .filter(w -> w.tenantId().equals(tenantId))
            .map(webhook -> {
                Webhook deactivated = webhookService.deactivate(id);
                return ResponseEntity.ok(WebhookResponse.from(deactivated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/rotate-secret")
    public ResponseEntity<WebhookSecretResponse> rotateSecret(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id) {

        return webhookService.findById(id)
            .filter(w -> w.tenantId().equals(tenantId))
            .map(webhook -> {
                // Generate a new secret and update the webhook
                String newSecret = webhookService.generateSecret();
                Webhook updateData = Webhook.builder()
                    .secret(newSecret)
                    .build();
                Webhook updated = webhookService.update(id, updateData);
                return ResponseEntity.ok(new WebhookSecretResponse(id, updated.secret()));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<WebhookDeliveryResponse> testWebhook(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id) {

        return webhookService.findById(id)
            .filter(w -> w.tenantId().equals(tenantId))
            .map(webhook -> {
                WebhookEvent testEvent = WebhookEvent.test(tenantId);
                WebhookDelivery delivery = deliveryService.deliver(webhook, testEvent);
                return ResponseEntity.ok(WebhookDeliveryResponse.from(delivery));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/deliveries")
    public ResponseEntity<List<WebhookDeliveryResponse>> getDeliveries(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id) {

        return webhookService.findById(id)
            .filter(w -> w.tenantId().equals(tenantId))
            .map(webhook -> {
                List<WebhookDeliveryResponse> deliveries = deliveryService.findByWebhookId(id)
                    .stream()
                    .map(WebhookDeliveryResponse::from)
                    .toList();
                return ResponseEntity.ok(deliveries);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/deliveries/{deliveryId}/retry")
    public ResponseEntity<WebhookDeliveryResponse> retryDelivery(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id,
            @PathVariable UUID deliveryId) {

        var webhookOpt = webhookService.findById(id)
            .filter(w -> w.tenantId().equals(tenantId));

        if (webhookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if delivery belongs to this webhook
        List<WebhookDelivery> deliveries = deliveryService.findByWebhookId(id);
        boolean belongsToWebhook = deliveries.stream()
            .anyMatch(d -> d.id().equals(deliveryId));

        if (!belongsToWebhook) {
            return ResponseEntity.notFound().build();
        }

        WebhookDelivery retried = deliveryService.retry(deliveryId);
        return ResponseEntity.ok(WebhookDeliveryResponse.from(retried));
    }
}
