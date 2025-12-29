package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TenantStatsTest {

    @Test
    void shouldBuildTenantStatsWithAllFields() {
        UUID tenantId = UUID.randomUUID();
        Instant now = Instant.now();
        Map<String, Long> eventsByType = Map.of("CREATE", 100L, "UPDATE", 50L);
        Map<String, Long> eventsBySource = Map.of("web", 120L, "api", 30L);

        TenantStats stats = TenantStats.builder()
                .tenantId(tenantId)
                .tenantName("Test Tenant")
                .totalEvents(10000L)
                .eventsToday(500L)
                .eventsThisMonth(15000L)
                .storageUsedBytes(1073741824L)
                .activeSources(5)
                .activeUsers(10)
                .activeApiKeys(8)
                .alertsTriggeredToday(3)
                .eventsByType(eventsByType)
                .eventsBySource(eventsBySource)
                .averageEventsPerDay(500.0)
                .lastEventAt(now)
                .calculatedAt(now)
                .build();

        assertThat(stats.getTenantId()).isEqualTo(tenantId);
        assertThat(stats.getTenantName()).isEqualTo("Test Tenant");
        assertThat(stats.getTotalEvents()).isEqualTo(10000L);
        assertThat(stats.getEventsToday()).isEqualTo(500L);
        assertThat(stats.getEventsThisMonth()).isEqualTo(15000L);
        assertThat(stats.getStorageUsedBytes()).isEqualTo(1073741824L);
        assertThat(stats.getActiveSources()).isEqualTo(5);
        assertThat(stats.getActiveUsers()).isEqualTo(10);
        assertThat(stats.getActiveApiKeys()).isEqualTo(8);
        assertThat(stats.getAlertsTriggeredToday()).isEqualTo(3);
        assertThat(stats.getEventsByType()).isEqualTo(eventsByType);
        assertThat(stats.getEventsBySource()).isEqualTo(eventsBySource);
        assertThat(stats.getAverageEventsPerDay()).isEqualTo(500.0);
        assertThat(stats.getLastEventAt()).isEqualTo(now);
        assertThat(stats.getCalculatedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateEmptyTenantStats() {
        TenantStats stats = new TenantStats();

        assertThat(stats.getTenantId()).isNull();
        assertThat(stats.getTenantName()).isNull();
        assertThat(stats.getTotalEvents()).isZero();
    }

    @Test
    void shouldSetAndGetTenantId() {
        TenantStats stats = new TenantStats();
        UUID tenantId = UUID.randomUUID();

        stats.setTenantId(tenantId);

        assertThat(stats.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    void shouldSetAndGetTenantName() {
        TenantStats stats = new TenantStats();

        stats.setTenantName("My Tenant");

        assertThat(stats.getTenantName()).isEqualTo("My Tenant");
    }

    @Test
    void shouldSetAndGetTotalEvents() {
        TenantStats stats = new TenantStats();

        stats.setTotalEvents(50000L);

        assertThat(stats.getTotalEvents()).isEqualTo(50000L);
    }

    @Test
    void shouldSetAndGetEventsToday() {
        TenantStats stats = new TenantStats();

        stats.setEventsToday(1000L);

        assertThat(stats.getEventsToday()).isEqualTo(1000L);
    }

    @Test
    void shouldSetAndGetEventsThisMonth() {
        TenantStats stats = new TenantStats();

        stats.setEventsThisMonth(30000L);

        assertThat(stats.getEventsThisMonth()).isEqualTo(30000L);
    }

    @Test
    void shouldSetAndGetStorageUsedBytes() {
        TenantStats stats = new TenantStats();

        stats.setStorageUsedBytes(2147483648L);

        assertThat(stats.getStorageUsedBytes()).isEqualTo(2147483648L);
    }

    @Test
    void shouldSetAndGetActiveSources() {
        TenantStats stats = new TenantStats();

        stats.setActiveSources(15);

        assertThat(stats.getActiveSources()).isEqualTo(15);
    }

    @Test
    void shouldSetAndGetActiveUsers() {
        TenantStats stats = new TenantStats();

        stats.setActiveUsers(25);

        assertThat(stats.getActiveUsers()).isEqualTo(25);
    }

    @Test
    void shouldSetAndGetActiveApiKeys() {
        TenantStats stats = new TenantStats();

        stats.setActiveApiKeys(20);

        assertThat(stats.getActiveApiKeys()).isEqualTo(20);
    }

    @Test
    void shouldSetAndGetAlertsTriggeredToday() {
        TenantStats stats = new TenantStats();

        stats.setAlertsTriggeredToday(7);

        assertThat(stats.getAlertsTriggeredToday()).isEqualTo(7);
    }

    @Test
    void shouldSetAndGetEventsByType() {
        TenantStats stats = new TenantStats();
        Map<String, Long> eventsByType = Map.of("DELETE", 100L);

        stats.setEventsByType(eventsByType);

        assertThat(stats.getEventsByType()).isEqualTo(eventsByType);
    }

    @Test
    void shouldSetAndGetEventsBySource() {
        TenantStats stats = new TenantStats();
        Map<String, Long> eventsBySource = Map.of("mobile", 200L);

        stats.setEventsBySource(eventsBySource);

        assertThat(stats.getEventsBySource()).isEqualTo(eventsBySource);
    }

    @Test
    void shouldSetAndGetAverageEventsPerDay() {
        TenantStats stats = new TenantStats();

        stats.setAverageEventsPerDay(750.5);

        assertThat(stats.getAverageEventsPerDay()).isEqualTo(750.5);
    }

    @Test
    void shouldSetAndGetLastEventAt() {
        TenantStats stats = new TenantStats();
        Instant now = Instant.now();

        stats.setLastEventAt(now);

        assertThat(stats.getLastEventAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetCalculatedAt() {
        TenantStats stats = new TenantStats();
        Instant now = Instant.now();

        stats.setCalculatedAt(now);

        assertThat(stats.getCalculatedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateTenantStatsFromFixtures() {
        TenantStats stats = AdminTestFixtures.createTenantStats();

        assertThat(stats).isNotNull();
        assertThat(stats.getTenantId()).isNotNull();
        assertThat(stats.getTenantName()).isNotBlank();
        assertThat(stats.getTotalEvents()).isPositive();
        assertThat(stats.getActiveSources()).isPositive();
        assertThat(stats.getEventsByType()).isNotEmpty();
        assertThat(stats.getEventsBySource()).isNotEmpty();
    }
}
