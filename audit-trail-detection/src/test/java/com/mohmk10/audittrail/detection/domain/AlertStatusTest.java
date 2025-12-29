package com.mohmk10.audittrail.detection.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertStatusTest {

    @Test
    void shouldSupportOpen() {
        assertThat(AlertStatus.OPEN).isNotNull();
        assertThat(AlertStatus.OPEN.name()).isEqualTo("OPEN");
    }

    @Test
    void shouldSupportAcknowledged() {
        assertThat(AlertStatus.ACKNOWLEDGED).isNotNull();
        assertThat(AlertStatus.ACKNOWLEDGED.name()).isEqualTo("ACKNOWLEDGED");
    }

    @Test
    void shouldSupportResolved() {
        assertThat(AlertStatus.RESOLVED).isNotNull();
        assertThat(AlertStatus.RESOLVED.name()).isEqualTo("RESOLVED");
    }

    @Test
    void shouldSupportDismissed() {
        assertThat(AlertStatus.DISMISSED).isNotNull();
        assertThat(AlertStatus.DISMISSED.name()).isEqualTo("DISMISSED");
    }

    @Test
    void shouldHaveFourStatuses() {
        assertThat(AlertStatus.values()).hasSize(4);
    }

    @Test
    void shouldParseFromString() {
        assertThat(AlertStatus.valueOf("OPEN")).isEqualTo(AlertStatus.OPEN);
        assertThat(AlertStatus.valueOf("ACKNOWLEDGED")).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(AlertStatus.valueOf("RESOLVED")).isEqualTo(AlertStatus.RESOLVED);
        assertThat(AlertStatus.valueOf("DISMISSED")).isEqualTo(AlertStatus.DISMISSED);
    }
}
