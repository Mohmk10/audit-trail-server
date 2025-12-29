package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class HealthStatusTest {

    @Test
    void shouldHaveFourStatusTypes() {
        assertThat(HealthStatus.values()).hasSize(4);
    }

    @Test
    void shouldContainHealthyStatus() {
        assertThat(HealthStatus.HEALTHY).isNotNull();
        assertThat(HealthStatus.HEALTHY.name()).isEqualTo("HEALTHY");
    }

    @Test
    void shouldContainDegradedStatus() {
        assertThat(HealthStatus.DEGRADED).isNotNull();
        assertThat(HealthStatus.DEGRADED.name()).isEqualTo("DEGRADED");
    }

    @Test
    void shouldContainUnhealthyStatus() {
        assertThat(HealthStatus.UNHEALTHY).isNotNull();
        assertThat(HealthStatus.UNHEALTHY.name()).isEqualTo("UNHEALTHY");
    }

    @Test
    void shouldContainUnknownStatus() {
        assertThat(HealthStatus.UNKNOWN).isNotNull();
        assertThat(HealthStatus.UNKNOWN.name()).isEqualTo("UNKNOWN");
    }

    @ParameterizedTest
    @EnumSource(HealthStatus.class)
    void shouldHaveValidName(HealthStatus status) {
        assertThat(status.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(HealthStatus.valueOf("HEALTHY")).isEqualTo(HealthStatus.HEALTHY);
        assertThat(HealthStatus.valueOf("DEGRADED")).isEqualTo(HealthStatus.DEGRADED);
        assertThat(HealthStatus.valueOf("UNHEALTHY")).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(HealthStatus.valueOf("UNKNOWN")).isEqualTo(HealthStatus.UNKNOWN);
    }
}
