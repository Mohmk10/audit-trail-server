package com.mohmk10.audittrail.integration.webhook.service;

import java.time.Instant;

public interface WebhookRetryService {
    void processRetries();
    Instant calculateNextRetry(int attemptCount);
}
