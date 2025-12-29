package com.mohmk10.audittrail.integration.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.JpaWebhookDeliveryRepository;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.JpaWebhookRepository;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookDeliveryEntity;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookDeliveryMapper;
import com.mohmk10.audittrail.integration.webhook.domain.*;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WebhookDeliveryServiceImpl implements WebhookDeliveryService {
    
    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryServiceImpl.class);
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    
    private final WebClient webClient;
    private final JpaWebhookDeliveryRepository deliveryRepository;
    private final JpaWebhookRepository webhookRepository;
    private final WebhookDeliveryMapper mapper;
    private final ObjectMapper objectMapper;
    private final WebhookRetryService retryService;
    
    public WebhookDeliveryServiceImpl(
            WebClient.Builder webClientBuilder,
            JpaWebhookDeliveryRepository deliveryRepository,
            JpaWebhookRepository webhookRepository,
            WebhookDeliveryMapper mapper,
            ObjectMapper objectMapper,
            WebhookRetryService retryService) {
        this.webClient = webClientBuilder.build();
        this.deliveryRepository = deliveryRepository;
        this.webhookRepository = webhookRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.retryService = retryService;
    }
    
    @Override
    public WebhookDelivery deliver(Webhook webhook, WebhookEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize webhook event", e);
        }
        
        String signature = calculateSignature(payload, webhook.secret());
        
        WebhookDeliveryEntity deliveryEntity = createPendingDelivery(webhook.id(), event.type(), payload);
        
        try {
            String responseBody = webClient.post()
                .uri(webhook.url())
                .header("Content-Type", "application/json")
                .header("X-Webhook-Signature", signature)
                .header("X-Webhook-Id", webhook.id().toString())
                .header("X-Event-Type", event.type())
                .header("X-Delivery-Id", deliveryEntity.getId().toString())
                .headers(h -> {
                    if (webhook.headers() != null) {
                        webhook.headers().forEach(h::add);
                    }
                })
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block(TIMEOUT);
            
            markDelivered(deliveryEntity, 200, responseBody);
            log.info("Webhook delivered successfully: webhookId={}, deliveryId={}", webhook.id(), deliveryEntity.getId());
            
        } catch (WebClientResponseException e) {
            markFailed(deliveryEntity, e.getStatusCode().value(), e.getResponseBodyAsString(), e.getMessage());
            scheduleRetryIfNeeded(deliveryEntity, webhook.maxRetries());
            log.warn("Webhook delivery failed: webhookId={}, status={}, error={}", 
                webhook.id(), e.getStatusCode().value(), e.getMessage());
                
        } catch (Exception e) {
            markFailed(deliveryEntity, null, null, e.getMessage());
            scheduleRetryIfNeeded(deliveryEntity, webhook.maxRetries());
            log.error("Webhook delivery error: webhookId={}, error={}", webhook.id(), e.getMessage());
        }
        
        return mapper.toDomain(deliveryEntity);
    }
    
    @Override
    @Async
    public void deliverAsync(Webhook webhook, WebhookEvent event) {
        deliver(webhook, event);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WebhookDelivery> findByWebhookId(UUID webhookId) {
        return deliveryRepository.findByWebhookIdOrderByCreatedAtDesc(webhookId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WebhookDelivery> findPendingRetries() {
        return deliveryRepository.findByStatusAndNextRetryAtBefore(DeliveryStatus.RETRYING, Instant.now()).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public WebhookDelivery retry(UUID deliveryId) {
        WebhookDeliveryEntity delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + deliveryId));
        
        var webhookEntity = webhookRepository.findById(delivery.getWebhookId())
            .orElseThrow(() -> new WebhookNotFoundException(delivery.getWebhookId()));
        
        Webhook webhook = new Webhook(
            webhookEntity.getId(),
            webhookEntity.getTenantId(),
            webhookEntity.getName(),
            webhookEntity.getUrl(),
            webhookEntity.getSecret(),
            webhookEntity.getEvents(),
            webhookEntity.getStatus(),
            webhookEntity.getHeaders(),
            webhookEntity.getMaxRetries(),
            webhookEntity.getCreatedAt(),
            webhookEntity.getUpdatedAt()
        );
        
        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        delivery.setStatus(DeliveryStatus.PENDING);
        deliveryRepository.save(delivery);
        
        try {
            WebhookEvent event = objectMapper.readValue(delivery.getEventPayload(), WebhookEvent.class);
            return deliver(webhook, event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event payload for retry", e);
        }
    }
    
    @Override
    public String calculateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(), HMAC_SHA256));
            byte[] hash = mac.doFinal(payload.getBytes());
            return "sha256=" + Hex.encodeHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC signature", e);
        }
    }
    
    private WebhookDeliveryEntity createPendingDelivery(UUID webhookId, String eventType, String payload) {
        WebhookDeliveryEntity entity = new WebhookDeliveryEntity();
        entity.setId(UUID.randomUUID());
        entity.setWebhookId(webhookId);
        entity.setEventType(eventType);
        entity.setEventPayload(payload);
        entity.setStatus(DeliveryStatus.PENDING);
        entity.setAttemptCount(0);
        entity.setCreatedAt(Instant.now());
        return deliveryRepository.save(entity);
    }
    
    private void markDelivered(WebhookDeliveryEntity entity, int httpStatus, String responseBody) {
        entity.setStatus(DeliveryStatus.DELIVERED);
        entity.setHttpStatus(httpStatus);
        entity.setResponseBody(truncate(responseBody, 1000));
        entity.setDeliveredAt(Instant.now());
        entity.setAttemptCount(entity.getAttemptCount() + 1);
        deliveryRepository.save(entity);
    }
    
    private void markFailed(WebhookDeliveryEntity entity, Integer httpStatus, String responseBody, String errorMessage) {
        entity.setStatus(DeliveryStatus.FAILED);
        entity.setHttpStatus(httpStatus);
        entity.setResponseBody(truncate(responseBody, 1000));
        entity.setErrorMessage(truncate(errorMessage, 500));
        entity.setAttemptCount(entity.getAttemptCount() + 1);
        deliveryRepository.save(entity);
    }
    
    private void scheduleRetryIfNeeded(WebhookDeliveryEntity entity, int maxRetries) {
        if (entity.getAttemptCount() < maxRetries) {
            entity.setStatus(DeliveryStatus.RETRYING);
            entity.setNextRetryAt(retryService.calculateNextRetry(entity.getAttemptCount()));
            deliveryRepository.save(entity);
            log.info("Scheduled retry: deliveryId={}, attemptCount={}, nextRetryAt={}", 
                entity.getId(), entity.getAttemptCount(), entity.getNextRetryAt());
        }
    }
    
    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) + "..." : value;
    }
}
