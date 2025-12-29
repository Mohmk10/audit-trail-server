package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SystemHealthTest {

    @Test
    void shouldBuildSystemHealthWithAllFields() {
        Instant now = Instant.now();
        Instant startTime = now.minusSeconds(3600);
        List<ComponentHealth> components = List.of(
                ComponentHealth.healthy("database"),
                ComponentHealth.healthy("cache")
        );
        Map<String, Object> metrics = Map.of("requestCount", 1000L);

        SystemHealth health = SystemHealth.builder()
                .overallStatus(HealthStatus.HEALTHY)
                .components(components)
                .version("1.0.0")
                .startTime(startTime)
                .uptimeSeconds(3600L)
                .metrics(metrics)
                .timestamp(now)
                .build();

        assertThat(health.getOverallStatus()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(health.getComponents()).isEqualTo(components);
        assertThat(health.getVersion()).isEqualTo("1.0.0");
        assertThat(health.getStartTime()).isEqualTo(startTime);
        assertThat(health.getUptimeSeconds()).isEqualTo(3600L);
        assertThat(health.getMetrics()).isEqualTo(metrics);
        assertThat(health.getTimestamp()).isEqualTo(now);
    }

    @Test
    void shouldCreateEmptySystemHealth() {
        SystemHealth health = new SystemHealth();

        assertThat(health.getOverallStatus()).isNull();
        assertThat(health.getComponents()).isNull();
        assertThat(health.getVersion()).isNull();
    }

    @Test
    void shouldSetAndGetOverallStatus() {
        SystemHealth health = new SystemHealth();

        health.setOverallStatus(HealthStatus.DEGRADED);

        assertThat(health.getOverallStatus()).isEqualTo(HealthStatus.DEGRADED);
    }

    @Test
    void shouldSetAndGetComponents() {
        SystemHealth health = new SystemHealth();
        List<ComponentHealth> components = List.of(ComponentHealth.healthy("test"));

        health.setComponents(components);

        assertThat(health.getComponents()).isEqualTo(components);
    }

    @Test
    void shouldSetAndGetVersion() {
        SystemHealth health = new SystemHealth();

        health.setVersion("2.0.0");

        assertThat(health.getVersion()).isEqualTo("2.0.0");
    }

    @Test
    void shouldSetAndGetStartTime() {
        SystemHealth health = new SystemHealth();
        Instant startTime = Instant.now();

        health.setStartTime(startTime);

        assertThat(health.getStartTime()).isEqualTo(startTime);
    }

    @Test
    void shouldSetAndGetUptimeSeconds() {
        SystemHealth health = new SystemHealth();

        health.setUptimeSeconds(7200L);

        assertThat(health.getUptimeSeconds()).isEqualTo(7200L);
    }

    @Test
    void shouldSetAndGetMetrics() {
        SystemHealth health = new SystemHealth();
        Map<String, Object> metrics = Map.of("cpu", 50.5);

        health.setMetrics(metrics);

        assertThat(health.getMetrics()).isEqualTo(metrics);
    }

    @Test
    void shouldSetAndGetTimestamp() {
        SystemHealth health = new SystemHealth();
        Instant now = Instant.now();

        health.setTimestamp(now);

        assertThat(health.getTimestamp()).isEqualTo(now);
    }

    @Test
    void shouldCreateSystemHealthFromFixtures() {
        SystemHealth health = AdminTestFixtures.createSystemHealth();

        assertThat(health).isNotNull();
        assertThat(health.getOverallStatus()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(health.getComponents()).isNotEmpty();
        assertThat(health.getVersion()).isNotBlank();
        assertThat(health.getUptimeSeconds()).isPositive();
    }

    @Test
    void shouldCreateDegradedSystemHealthFromFixtures() {
        SystemHealth health = AdminTestFixtures.createDegradedSystemHealth();

        assertThat(health).isNotNull();
        assertThat(health.getOverallStatus()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(health.getComponents()).isNotEmpty();
    }

    @Test
    void shouldContainHealthyComponents() {
        SystemHealth health = AdminTestFixtures.createSystemHealth();

        long healthyCount = health.getComponents().stream()
                .filter(c -> c.getStatus() == HealthStatus.HEALTHY)
                .count();

        assertThat(healthyCount).isEqualTo(health.getComponents().size());
    }

    @Test
    void shouldContainUnhealthyComponentInDegradedHealth() {
        SystemHealth health = AdminTestFixtures.createDegradedSystemHealth();

        boolean hasUnhealthy = health.getComponents().stream()
                .anyMatch(c -> c.getStatus() == HealthStatus.UNHEALTHY);

        assertThat(hasUnhealthy).isTrue();
    }
}
