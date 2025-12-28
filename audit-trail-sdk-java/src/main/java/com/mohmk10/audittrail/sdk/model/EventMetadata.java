package com.mohmk10.audittrail.sdk.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EventMetadata {
    private String source;
    private String tenantId;
    private String correlationId;
    private String sessionId;
    private Map<String, String> tags;
    private Map<String, Object> extra;

    private EventMetadata() {}

    private EventMetadata(EventMetadataBuilder builder) {
        this.source = builder.source;
        this.tenantId = builder.tenantId;
        this.correlationId = builder.correlationId;
        this.sessionId = builder.sessionId;
        this.tags = builder.tags.isEmpty() ? null : builder.tags;
        this.extra = builder.extra.isEmpty() ? null : builder.extra;
    }

    public static EventMetadataBuilder builder() {
        return new EventMetadataBuilder();
    }

    public String getSource() {
        return source;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventMetadata that = (EventMetadata) o;
        return Objects.equals(source, that.source) && Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, tenantId);
    }

    public static class EventMetadataBuilder {
        private String source;
        private String tenantId;
        private String correlationId;
        private String sessionId;
        private Map<String, String> tags = new HashMap<>();
        private Map<String, Object> extra = new HashMap<>();

        public EventMetadataBuilder source(String source) {
            this.source = source;
            return this;
        }

        public EventMetadataBuilder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public EventMetadataBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public EventMetadataBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public EventMetadataBuilder tag(String key, String value) {
            this.tags.put(key, value);
            return this;
        }

        public EventMetadataBuilder tags(Map<String, String> tags) {
            this.tags.putAll(tags);
            return this;
        }

        public EventMetadataBuilder extra(String key, Object value) {
            this.extra.put(key, value);
            return this;
        }

        public EventMetadataBuilder extra(Map<String, Object> extra) {
            this.extra.putAll(extra);
            return this;
        }

        public EventMetadata build() {
            return new EventMetadata(this);
        }
    }
}
