package com.mohmk10.audittrail.integration.exporter.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit-trail.exporter.s3")
public class S3Properties {

    private boolean enabled = false;
    private String bucket;
    private String prefix = "audit-events";
    private String region = "us-east-1";
    private int batchSize = 1000;
    private int flushIntervalSeconds = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getFlushIntervalSeconds() {
        return flushIntervalSeconds;
    }

    public void setFlushIntervalSeconds(int flushIntervalSeconds) {
        this.flushIntervalSeconds = flushIntervalSeconds;
    }
}
