package com.mohmk10.audittrail.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohmk10.audittrail.integration.webhook.adapter.in.event.WebhookEventListener;
import com.mohmk10.audittrail.integration.webhook.adapter.in.rest.WebhookController;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.JpaWebhookDeliveryRepository;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.JpaWebhookRepository;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookDeliveryMapper;
import com.mohmk10.audittrail.integration.webhook.adapter.out.persistence.WebhookMapper;
import com.mohmk10.audittrail.integration.webhook.service.WebhookDeliveryService;
import com.mohmk10.audittrail.integration.webhook.service.WebhookDeliveryServiceImpl;
import com.mohmk10.audittrail.integration.webhook.service.WebhookRetryService;
import com.mohmk10.audittrail.integration.webhook.service.WebhookRetryServiceImpl;
import com.mohmk10.audittrail.integration.webhook.service.WebhookService;
import com.mohmk10.audittrail.integration.webhook.service.WebhookServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
@EnableAsync
@EnableScheduling
@ConditionalOnProperty(prefix = "audit-trail.webhook", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AuditTrailIntegrationProperties.class)
public class WebhookAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebhookMapper webhookMapper() {
        return new WebhookMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebhookDeliveryMapper webhookDeliveryMapper() {
        return new WebhookDeliveryMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebhookService webhookService(
            JpaWebhookRepository webhookRepository,
            WebhookMapper webhookMapper) {
        return new WebhookServiceImpl(webhookRepository, webhookMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebhookRetryService webhookRetryService(
            @Lazy WebhookDeliveryService deliveryService) {
        return new WebhookRetryServiceImpl(deliveryService);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebhookDeliveryService webhookDeliveryService(
            WebClient.Builder webClientBuilder,
            JpaWebhookDeliveryRepository deliveryRepository,
            JpaWebhookRepository webhookRepository,
            WebhookDeliveryMapper deliveryMapper,
            ObjectMapper objectMapper,
            WebhookRetryService retryService) {
        return new WebhookDeliveryServiceImpl(
            webClientBuilder, deliveryRepository, webhookRepository, deliveryMapper, objectMapper, retryService);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebhookEventListener webhookEventListener(
            WebhookService webhookService,
            WebhookDeliveryService deliveryService) {
        return new WebhookEventListener(webhookService, deliveryService);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebhookController webhookController(
            WebhookService webhookService,
            WebhookDeliveryService deliveryService) {
        return new WebhookController(webhookService, deliveryService);
    }
}
