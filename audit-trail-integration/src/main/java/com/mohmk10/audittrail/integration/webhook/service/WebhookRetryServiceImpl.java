package com.mohmk10.audittrail.integration.webhook.service;

import com.mohmk10.audittrail.integration.webhook.domain.WebhookDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WebhookRetryServiceImpl implements WebhookRetryService {
    
    private static final Logger log = LoggerFactory.getLogger(WebhookRetryServiceImpl.class);
    
    // Exponential backoff delays in seconds: 1min, 5min, 15min, 1h, 4h
    private static final long[] RETRY_DELAYS_SECONDS = {60, 300, 900, 3600, 14400};
    
    private final WebhookDeliveryService deliveryService;
    
    public WebhookRetryServiceImpl(WebhookDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    @Override
    @Scheduled(fixedRate = 60000) // Every minute
    public void processRetries() {
        List<WebhookDelivery> pending = deliveryService.findPendingRetries();
        
        if (!pending.isEmpty()) {
            log.info("Processing {} pending webhook retries", pending.size());
        }
        
        for (WebhookDelivery delivery : pending) {
            try {
                deliveryService.retry(delivery.id());
            } catch (Exception e) {
                log.error("Failed to retry delivery {}: {}", delivery.id(), e.getMessage());
            }
        }
    }
    
    @Override
    public Instant calculateNextRetry(int attemptCount) {
        int index = Math.min(attemptCount, RETRY_DELAYS_SECONDS.length - 1);
        return Instant.now().plusSeconds(RETRY_DELAYS_SECONDS[index]);
    }
}
