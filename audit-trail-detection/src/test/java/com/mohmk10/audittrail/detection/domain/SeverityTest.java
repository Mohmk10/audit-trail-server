package com.mohmk10.audittrail.detection.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeverityTest {

    @Test
    void shouldSupportLow() {
        assertThat(Severity.LOW).isNotNull();
        assertThat(Severity.LOW.name()).isEqualTo("LOW");
    }

    @Test
    void shouldSupportMedium() {
        assertThat(Severity.MEDIUM).isNotNull();
        assertThat(Severity.MEDIUM.name()).isEqualTo("MEDIUM");
    }

    @Test
    void shouldSupportHigh() {
        assertThat(Severity.HIGH).isNotNull();
        assertThat(Severity.HIGH.name()).isEqualTo("HIGH");
    }

    @Test
    void shouldSupportCritical() {
        assertThat(Severity.CRITICAL).isNotNull();
        assertThat(Severity.CRITICAL.name()).isEqualTo("CRITICAL");
    }

    @Test
    void shouldHaveFourLevels() {
        assertThat(Severity.values()).hasSize(4);
    }

    @Test
    void shouldParseFromString() {
        assertThat(Severity.valueOf("LOW")).isEqualTo(Severity.LOW);
        assertThat(Severity.valueOf("MEDIUM")).isEqualTo(Severity.MEDIUM);
        assertThat(Severity.valueOf("HIGH")).isEqualTo(Severity.HIGH);
        assertThat(Severity.valueOf("CRITICAL")).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void shouldOrderBySeverity() {
        assertThat(Severity.LOW.ordinal()).isLessThan(Severity.MEDIUM.ordinal());
        assertThat(Severity.MEDIUM.ordinal()).isLessThan(Severity.HIGH.ordinal());
        assertThat(Severity.HIGH.ordinal()).isLessThan(Severity.CRITICAL.ordinal());
    }
}
