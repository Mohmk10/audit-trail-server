package com.mohmk10.audittrail.sdk.config;

import java.time.Duration;
import java.util.Objects;

public class AuditTrailConfig {
    private final String serverUrl;
    private final String apiKey;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final int maxRetries;
    private final Duration retryDelay;
    private final boolean asyncEnabled;
    private final int batchSize;
    private final Duration batchFlushInterval;

    private AuditTrailConfig(AuditTrailConfigBuilder builder) {
        this.serverUrl = builder.serverUrl;
        this.apiKey = builder.apiKey;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.maxRetries = builder.maxRetries;
        this.retryDelay = builder.retryDelay;
        this.asyncEnabled = builder.asyncEnabled;
        this.batchSize = builder.batchSize;
        this.batchFlushInterval = builder.batchFlushInterval;
    }

    public static AuditTrailConfigBuilder builder() {
        return new AuditTrailConfigBuilder();
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public Duration getBatchFlushInterval() {
        return batchFlushInterval;
    }

    public static class AuditTrailConfigBuilder {
        private String serverUrl;
        private String apiKey;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
        private int maxRetries = 3;
        private Duration retryDelay = Duration.ofMillis(500);
        private boolean asyncEnabled = true;
        private int batchSize = 100;
        private Duration batchFlushInterval = Duration.ofSeconds(5);

        public AuditTrailConfigBuilder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public AuditTrailConfigBuilder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public AuditTrailConfigBuilder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public AuditTrailConfigBuilder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public AuditTrailConfigBuilder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public AuditTrailConfigBuilder retryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public AuditTrailConfigBuilder asyncEnabled(boolean asyncEnabled) {
            this.asyncEnabled = asyncEnabled;
            return this;
        }

        public AuditTrailConfigBuilder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public AuditTrailConfigBuilder batchFlushInterval(Duration batchFlushInterval) {
            this.batchFlushInterval = batchFlushInterval;
            return this;
        }

        public AuditTrailConfig build() {
            Objects.requireNonNull(serverUrl, "Server URL is required");
            return new AuditTrailConfig(this);
        }
    }
}
