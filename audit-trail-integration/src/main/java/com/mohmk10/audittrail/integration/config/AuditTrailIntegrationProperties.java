package com.mohmk10.audittrail.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit-trail")
public class AuditTrailIntegrationProperties {

    private Webhook webhook = new Webhook();
    private RabbitMQ rabbitmq = new RabbitMQ();
    private Exporter exporter = new Exporter();

    public Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }

    public RabbitMQ getRabbitmq() {
        return rabbitmq;
    }

    public void setRabbitmq(RabbitMQ rabbitmq) {
        this.rabbitmq = rabbitmq;
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

    public static class RabbitMQ {
        private boolean enabled = false;
        private Queue queue = new Queue();
        private Exchange exchange = new Exchange();
        private RoutingKey routingKey = new RoutingKey();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Queue getQueue() {
            return queue;
        }

        public void setQueue(Queue queue) {
            this.queue = queue;
        }

        public Exchange getExchange() {
            return exchange;
        }

        public void setExchange(Exchange exchange) {
            this.exchange = exchange;
        }

        public RoutingKey getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(RoutingKey routingKey) {
            this.routingKey = routingKey;
        }

        public static class Queue {
            private String events = "audit-events-queue";

            public String getEvents() {
                return events;
            }

            public void setEvents(String events) {
                this.events = events;
            }
        }

        public static class Exchange {
            private String events = "audit-events-exchange";

            public String getEvents() {
                return events;
            }

            public void setEvents(String events) {
                this.events = events;
            }
        }

        public static class RoutingKey {
            private String events = "audit.events";

            public String getEvents() {
                return events;
            }

            public void setEvents(String events) {
                this.events = events;
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
