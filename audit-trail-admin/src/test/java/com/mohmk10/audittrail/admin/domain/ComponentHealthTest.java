package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentHealthTest {

    @Test
    void shouldBuildComponentHealthWithAllFields() {
        Instant now = Instant.now();
        Map<String, Object> details = Map.of("connections", 10);

        ComponentHealth health = ComponentHealth.builder()
                .name("database")
                .status(HealthStatus.HEALTHY)
                .message("Connected successfully")
                .responseTimeMs(15L)
                .details(details)
                .lastChecked(now)
                .build();

        assertThat(health.getName()).isEqualTo("database");
        assertThat(health.getStatus()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(health.getMessage()).isEqualTo("Connected successfully");
        assertThat(health.getResponseTimeMs()).isEqualTo(15L);
        assertThat(health.getDetails()).isEqualTo(details);
        assertThat(health.getLastChecked()).isEqualTo(now);
    }

    @Test
    void shouldCreateEmptyComponentHealth() {
        ComponentHealth health = new ComponentHealth();

        assertThat(health.getName()).isNull();
        assertThat(health.getStatus()).isNull();
        assertThat(health.getResponseTimeMs()).isZero();
    }

    @Test
    void shouldCreateHealthyComponentWithFactoryMethod() {
        ComponentHealth health = ComponentHealth.healthy("redis");

        assertThat(health.getName()).isEqualTo("redis");
        assertThat(health.getStatus()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(health.getLastChecked()).isNotNull();
    }

    @Test
    void shouldCreateUnhealthyComponentWithFactoryMethod() {
        ComponentHealth health = ComponentHealth.unhealthy("elasticsearch", "Connection refused");

        assertThat(health.getName()).isEqualTo("elasticsearch");
        assertThat(health.getStatus()).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(health.getMessage()).isEqualTo("Connection refused");
        assertThat(health.getLastChecked()).isNotNull();
    }

    @Test
    void shouldSetAndGetName() {
        ComponentHealth health = new ComponentHealth();

        health.setName("cache");

        assertThat(health.getName()).isEqualTo("cache");
    }

    @Test
    void shouldSetAndGetStatus() {
        ComponentHealth health = new ComponentHealth();

        health.setStatus(HealthStatus.DEGRADED);

        assertThat(health.getStatus()).isEqualTo(HealthStatus.DEGRADED);
    }

    @Test
    void shouldSetAndGetMessage() {
        ComponentHealth health = new ComponentHealth();

        health.setMessage("High latency detected");

        assertThat(health.getMessage()).isEqualTo("High latency detected");
    }

    @Test
    void shouldSetAndGetResponseTimeMs() {
        ComponentHealth health = new ComponentHealth();

        health.setResponseTimeMs(250L);

        assertThat(health.getResponseTimeMs()).isEqualTo(250L);
    }

    @Test
    void shouldSetAndGetDetails() {
        ComponentHealth health = new ComponentHealth();
        Map<String, Object> details = Map.of("poolSize", 20);

        health.setDetails(details);

        assertThat(health.getDetails()).isEqualTo(details);
    }

    @Test
    void shouldSetAndGetLastChecked() {
        ComponentHealth health = new ComponentHealth();
        Instant now = Instant.now();

        health.setLastChecked(now);

        assertThat(health.getLastChecked()).isEqualTo(now);
    }

    @Test
    void shouldCreateHealthyComponentFromFixtures() {
        ComponentHealth health = AdminTestFixtures.createHealthyComponent("test-component");

        assertThat(health.getName()).isEqualTo("test-component");
        assertThat(health.getStatus()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void shouldCreateUnhealthyComponentFromFixtures() {
        ComponentHealth health = AdminTestFixtures.createUnhealthyComponent("failed-component", "Error message");

        assertThat(health.getName()).isEqualTo("failed-component");
        assertThat(health.getStatus()).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(health.getMessage()).isEqualTo("Error message");
    }
}
