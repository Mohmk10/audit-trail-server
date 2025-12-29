package com.mohmk10.audittrail.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit-trail")
public class AuditTrailIntegrationProperties {

    private Webhook webhook = new Webhook();
    private Kafka kafka = new Kafka();
    private Exporter exporter = new Exporter();

    public Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }

    public Exporter getExporter() {
        return exporter;
    }

    public void setExporter(Exporter exporter) {
        this.exporter = exporter;
    }

    public static class Webhook {
        private boolean enabled = true;
        private int retryIntervalSeconds = 60;
        private int maxRetries = 5;
        private int deliveryTimeoutSeconds = 30;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRetryIntervalSeconds() {
            return retryIntervalSeconds;
        }

        public void setRetryIntervalSeconds(int retryIntervalSeconds) {
            this.retryIntervalSeconds = retryIntervalSeconds;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public int getDeliveryTimeoutSeconds() {
            return deliveryTimeoutSeconds;
        }

        public void setDeliveryTimeoutSeconds(int deliveryTimeoutSeconds) {
            this.deliveryTimeoutSeconds = deliveryTimeoutSeconds;
        }
    }

    public static class Kafka {
        private boolean enabled = false;
        private String topic = "audit-events";
        private Consumer consumer = new Consumer();
        private Producer producer = new Producer();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public Consumer getConsumer() {
            return consumer;
        }

        public void setConsumer(Consumer consumer) {
            this.consumer = consumer;
        }

        public Producer getProducer() {
            return producer;
        }

        public void setProducer(Producer producer) {
            this.producer = producer;
        }

        public static class Consumer {
            private String groupId = "audit-trail-group";
            private String autoOffsetReset = "earliest";
            private int maxPollRecords = 500;

            public String getGroupId() {
                return groupId;
            }

            public void setGroupId(String groupId) {
                this.groupId = groupId;
            }

            public String getAutoOffsetReset() {
                return autoOffsetReset;
            }

            public void setAutoOffsetReset(String autoOffsetReset) {
                this.autoOffsetReset = autoOffsetReset;
            }

            public int getMaxPollRecords() {
                return maxPollRecords;
            }

            public void setMaxPollRecords(int maxPollRecords) {
                this.maxPollRecords = maxPollRecords;
            }
        }

        public static class Producer {
            private String acks = "all";
            private int retries = 3;

            public String getAcks() {
                return acks;
            }

            public void setAcks(String acks) {
                this.acks = acks;
            }

            public int getRetries() {
                return retries;
            }

            public void setRetries(int retries) {
                this.retries = retries;
            }
        }
    }

    public static class Exporter {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
