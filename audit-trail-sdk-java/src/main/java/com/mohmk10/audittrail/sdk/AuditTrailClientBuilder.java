package com.mohmk10.audittrail.sdk;

import java.time.Duration;
import java.util.Objects;

import com.mohmk10.audittrail.sdk.config.AuditTrailConfig;

public class AuditTrailClientBuilder {
    private String serverUrl;
    private String apiKey;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);
    private int maxRetries = 3;
    private Duration retryDelay = Duration.ofMillis(500);
    private boolean asyncEnabled = true;

    public AuditTrailClientBuilder serverUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public AuditTrailClientBuilder apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public AuditTrailClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public AuditTrailClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public AuditTrailClientBuilder maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public AuditTrailClientBuilder retryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }

    public AuditTrailClientBuilder async(boolean enabled) {
        this.asyncEnabled = enabled;
        return this;
    }

    public AuditTrailClient build() {
        Objects.requireNonNull(serverUrl, "Server URL is required");

        AuditTrailConfig config = AuditTrailConfig.builder()
                .serverUrl(serverUrl)
                .apiKey(apiKey)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .maxRetries(maxRetries)
                .retryDelay(retryDelay)
                .asyncEnabled(asyncEnabled)
                .build();

        return new DefaultAuditTrailClient(config);
    }
}
