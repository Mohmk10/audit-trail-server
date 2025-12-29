package com.mohmk10.audittrail.integration.webhook.service;

import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.JpaWebhookRepository;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookEntity;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookMapper;
import com.mohmk10.audittrail.integration.webhook.domain.Webhook;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookNotFoundException;
import com.mohmk10.audittrail.integration.webhook.domain.WebhookStatus;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class WebhookServiceImpl implements WebhookService {
    
    private static final Logger log = LoggerFactory.getLogger(WebhookServiceImpl.class);
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    private final JpaWebhookRepository repository;
    private final WebhookMapper mapper;
    
    public WebhookServiceImpl(JpaWebhookRepository repository, WebhookMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    @Override
    public Webhook create(Webhook webhook) {
        Webhook toCreate = Webhook.builder()
            .id(webhook.id() != null ? webhook.id() : UUID.randomUUID())
            .tenantId(webhook.tenantId())
            .name(webhook.name())
            .url(webhook.url())
            .secret(webhook.secret() != null ? webhook.secret() : generateSecret())
            .events(webhook.events())
            .status(webhook.status() != null ? webhook.status() : WebhookStatus.ACTIVE)
            .headers(webhook.headers())
            .maxRetries(webhook.maxRetries() > 0 ? webhook.maxRetries() : Webhook.DEFAULT_MAX_RETRIES)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        
        WebhookEntity entity = mapper.toEntity(toCreate);
        WebhookEntity saved = repository.save(entity);
        log.info("Created webhook: id={}, name={}, tenantId={}", saved.getId(), saved.getName(), saved.getTenantId());
        return mapper.toDomain(saved);
    }
    
    @Override
    public Webhook update(UUID id, Webhook webhook) {
        WebhookEntity entity = repository.findById(id)
            .orElseThrow(() -> new WebhookNotFoundException(id));

        if (webhook.name() != null) {
            entity.setName(webhook.name());
        }
        if (webhook.url() != null) {
            entity.setUrl(webhook.url());
        }
        if (webhook.events() != null && !webhook.events().isEmpty()) {
            entity.setEvents(webhook.events());
        }
        if (webhook.headers() != null) {
            entity.setHeaders(webhook.headers());
        }
        if (webhook.maxRetries() > 0) {
            entity.setMaxRetries(webhook.maxRetries());
        }
        if (webhook.secret() != null) {
            entity.setSecret(webhook.secret());
        }
        entity.setUpdatedAt(Instant.now());

        WebhookEntity saved = repository.save(entity);
        log.info("Updated webhook: id={}", id);
        return mapper.toDomain(saved);
    }
    
    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new WebhookNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Deleted webhook: id={}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Webhook> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Webhook> findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Webhook> findActiveByTenantId(String tenantId) {
        return repository.findByTenantIdAndStatus(tenantId, WebhookStatus.ACTIVE).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public Webhook activate(UUID id) {
        return updateStatus(id, WebhookStatus.ACTIVE);
    }
    
    @Override
    public Webhook deactivate(UUID id) {
        return updateStatus(id, WebhookStatus.INACTIVE);
    }
    
    @Override
    public Webhook suspend(UUID id) {
        return updateStatus(id, WebhookStatus.SUSPENDED);
    }
    
    private Webhook updateStatus(UUID id, WebhookStatus status) {
        WebhookEntity entity = repository.findById(id)
            .orElseThrow(() -> new WebhookNotFoundException(id));
        entity.setStatus(status);
        entity.setUpdatedAt(Instant.now());
        WebhookEntity saved = repository.save(entity);
        log.info("Webhook status updated: id={}, status={}", id, status);
        return mapper.toDomain(saved);
    }
    
    @Override
    public String generateSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "whsec_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    @Override
    public boolean verifySignature(String payload, String signature, String secret) {
        try {
            String expectedSignature = calculateHmacSignature(payload, secret);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Failed to verify signature: {}", e.getMessage());
            return false;
        }
    }
    
    private String calculateHmacSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(), HMAC_SHA256));
            byte[] hash = mac.doFinal(payload.getBytes());
            return "sha256=" + Hex.encodeHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC signature", e);
        }
    }
}
