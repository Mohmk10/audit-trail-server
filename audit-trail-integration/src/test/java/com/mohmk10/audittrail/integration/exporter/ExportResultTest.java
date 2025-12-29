package com.mohmk10.audittrail.integration.exporter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExportResult Tests")
class ExportResultTest {

    @Nested
    @DisplayName("success() Factory Methods")
    class SuccessFactoryMethodTests {

        @Test
        @DisplayName("Should create success result with count")
        void shouldCreateSuccessResultWithCount() {
            Instant before = Instant.now();

            ExportResult result = ExportResult.success("splunk", 10);

            assertThat(result.exporterName()).isEqualTo("splunk");
            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(10);
            assertThat(result.message()).isEqualTo("Export successful");
            assertThat(result.timestamp()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("Should create success result with message")
        void shouldCreateSuccessResultWithMessage() {
            ExportResult result = ExportResult.success("s3", 100, "Exported to bucket");

            assertThat(result.exporterName()).isEqualTo("s3");
            assertThat(result.success()).isTrue();
            assertThat(result.eventsProcessed()).isEqualTo(100);
            assertThat(result.message()).isEqualTo("Exported to bucket");
        }
    }

    @Nested
    @DisplayName("failure() Factory Methods")
    class FailureFactoryMethodTests {

        @Test
        @DisplayName("Should create failure result with message")
        void shouldCreateFailureResultWithMessage() {
            Instant before = Instant.now();

            ExportResult result = ExportResult.failure("elk", "Connection timeout");

            assertThat(result.exporterName()).isEqualTo("elk");
            assertThat(result.success()).isFalse();
            assertThat(result.eventsProcessed()).isZero();
            assertThat(result.message()).isEqualTo("Connection timeout");
            assertThat(result.timestamp()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("Should create failure result with partial count")
        void shouldCreateFailureResultWithPartialCount() {
            ExportResult result = ExportResult.failure("splunk", 5, "Partial failure");

            assertThat(result.exporterName()).isEqualTo("splunk");
            assertThat(result.success()).isFalse();
            assertThat(result.eventsProcessed()).isEqualTo(5);
            assertThat(result.message()).isEqualTo("Partial failure");
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class RecordEqualityTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            Instant timestamp = Instant.now();
            ExportResult result1 = new ExportResult("splunk", true, 10, "Success", timestamp);
            ExportResult result2 = new ExportResult("splunk", true, 10, "Success", timestamp);

            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when success differs")
        void shouldNotBeEqualWhenSuccessDiffers() {
            Instant timestamp = Instant.now();
            ExportResult result1 = new ExportResult("splunk", true, 10, "Success", timestamp);
            ExportResult result2 = new ExportResult("splunk", false, 10, "Success", timestamp);

            assertThat(result1).isNotEqualTo(result2);
        }
    }
}
