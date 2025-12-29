package com.mohmk10.audittrail.integration.webhook.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebhookEvent Domain Tests")
class WebhookEventTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create event with of() factory method")
        void shouldCreateEventWithOfMethod() {
            Map<String, Object> payload = Map.of("key", "value");
            Instant before = Instant.now();

            WebhookEvent event = WebhookEvent.of(
                WebhookEvent.EVENT_STORED,
                "tenant-1",
                payload
            );

            Instant after = Instant.now();

            assertThat(event.type()).isEqualTo(WebhookEvent.EVENT_STORED);
            assertThat(event.tenantId()).isEqualTo("tenant-1");
            assertThat(event.payload()).containsEntry("key", "value");
            assertThat(event.timestamp())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("Should create test event with test() factory method")
        void shouldCreateTestEvent() {
            Instant before = Instant.now();

            WebhookEvent event = WebhookEvent.test("tenant-1");

            Instant after = Instant.now();

            assertThat(event.type()).isEqualTo(WebhookEvent.TEST);
            assertThat(event.tenantId()).isEqualTo("tenant-1");
            assertThat(event.payload()).containsEntry("message", "This is a test webhook event");
            assertThat(event.timestamp())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
        }
    }

    @Nested
    @DisplayName("Event Type Constants Tests")
    class EventTypeConstantsTests {

        @Test
        @DisplayName("Should have correct EVENT_STORED constant")
        void shouldHaveEventStoredConstant() {
            assertThat(WebhookEvent.EVENT_STORED).isEqualTo("event.stored");
        }

        @Test
        @DisplayName("Should have correct ALERT_CREATED constant")
        void shouldHaveAlertCreatedConstant() {
            assertThat(WebhookEvent.ALERT_CREATED).isEqualTo("alert.created");
        }

        @Test
        @DisplayName("Should have correct REPORT_GENERATED constant")
        void shouldHaveReportGeneratedConstant() {
            assertThat(WebhookEvent.REPORT_GENERATED).isEqualTo("report.generated");
        }

        @Test
        @DisplayName("Should have correct TEST constant")
        void shouldHaveTestConstant() {
            assertThat(WebhookEvent.TEST).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class RecordEqualityTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            Instant timestamp = Instant.now();
            Map<String, Object> payload = Map.of("key", "value");

            WebhookEvent event1 = new WebhookEvent("event.stored", "tenant-1", timestamp, payload);
            WebhookEvent event2 = new WebhookEvent("event.stored", "tenant-1", timestamp, payload);

            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when type differs")
        void shouldNotBeEqualWhenTypeDiffers() {
            Instant timestamp = Instant.now();
            Map<String, Object> payload = Map.of("key", "value");

            WebhookEvent event1 = new WebhookEvent("event.stored", "tenant-1", timestamp, payload);
            WebhookEvent event2 = new WebhookEvent("alert.created", "tenant-1", timestamp, payload);

            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("Should not be equal when tenantId differs")
        void shouldNotBeEqualWhenTenantIdDiffers() {
            Instant timestamp = Instant.now();
            Map<String, Object> payload = Map.of("key", "value");

            WebhookEvent event1 = new WebhookEvent("event.stored", "tenant-1", timestamp, payload);
            WebhookEvent event2 = new WebhookEvent("event.stored", "tenant-2", timestamp, payload);

            assertThat(event1).isNotEqualTo(event2);
        }
    }
}
