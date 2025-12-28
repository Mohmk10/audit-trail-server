package com.mohmk10.audittrail.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ingestion")
public class IngestionConfig {

    private BatchConfig batch = new BatchConfig();
    private ValidationConfig validation = new ValidationConfig();

    public BatchConfig getBatch() {
        return batch;
    }

    public void setBatch(BatchConfig batch) {
        this.batch = batch;
    }

    public ValidationConfig getValidation() {
        return validation;
    }

    public void setValidation(ValidationConfig validation) {
        this.validation = validation;
    }

    public static class BatchConfig {
        private int maxSize = 1000;

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
    }

    public static class ValidationConfig {
        private boolean strictMode = false;

        public boolean isStrictMode() {
            return strictMode;
        }

        public void setStrictMode(boolean strictMode) {
            this.strictMode = strictMode;
        }
    }
}
