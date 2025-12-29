package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TenantTest {

    @Test
    void shouldBuildTenantWithAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        TenantQuota quota = AdminTestFixtures.createTenantQuota();
        Map<String, String> settings = Map.of("timezone", "UTC");

        Tenant tenant = Tenant.builder()
                .id(id)
                .name("Test Tenant")
                .slug("test-tenant")
                .description("A test tenant")
                .status(TenantStatus.ACTIVE)
                .plan(TenantPlan.PRO)
                .quota(quota)
                .settings(settings)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(tenant.getId()).isEqualTo(id);
        assertThat(tenant.getName()).isEqualTo("Test Tenant");
        assertThat(tenant.getSlug()).isEqualTo("test-tenant");
        assertThat(tenant.getDescription()).isEqualTo("A test tenant");
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.getPlan()).isEqualTo(TenantPlan.PRO);
        assertThat(tenant.getQuota()).isEqualTo(quota);
        assertThat(tenant.getSettings()).isEqualTo(settings);
        assertThat(tenant.getCreatedAt()).isEqualTo(now);
        assertThat(tenant.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldBuildTenantWithMinimalFields() {
        Tenant tenant = Tenant.builder()
                .name("Minimal Tenant")
                .slug("minimal")
                .build();

        assertThat(tenant.getName()).isEqualTo("Minimal Tenant");
        assertThat(tenant.getSlug()).isEqualTo("minimal");
        assertThat(tenant.getId()).isNull();
        assertThat(tenant.getStatus()).isNull();
    }

    @Test
    void shouldCreateEmptyTenant() {
        Tenant tenant = new Tenant();

        assertThat(tenant.getId()).isNull();
        assertThat(tenant.getName()).isNull();
        assertThat(tenant.getSlug()).isNull();
    }

    @Test
    void shouldSetAndGetId() {
        Tenant tenant = new Tenant();
        UUID id = UUID.randomUUID();

        tenant.setId(id);

        assertThat(tenant.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetName() {
        Tenant tenant = new Tenant();

        tenant.setName("New Name");

        assertThat(tenant.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldSetAndGetSlug() {
        Tenant tenant = new Tenant();

        tenant.setSlug("new-slug");

        assertThat(tenant.getSlug()).isEqualTo("new-slug");
    }

    @Test
    void shouldSetAndGetDescription() {
        Tenant tenant = new Tenant();

        tenant.setDescription("New Description");

        assertThat(tenant.getDescription()).isEqualTo("New Description");
    }

    @Test
    void shouldSetAndGetStatus() {
        Tenant tenant = new Tenant();

        tenant.setStatus(TenantStatus.SUSPENDED);

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
    }

    @Test
    void shouldSetAndGetPlan() {
        Tenant tenant = new Tenant();

        tenant.setPlan(TenantPlan.ENTERPRISE);

        assertThat(tenant.getPlan()).isEqualTo(TenantPlan.ENTERPRISE);
    }

    @Test
    void shouldSetAndGetQuota() {
        Tenant tenant = new Tenant();
        TenantQuota quota = AdminTestFixtures.createTenantQuota();

        tenant.setQuota(quota);

        assertThat(tenant.getQuota()).isEqualTo(quota);
    }

    @Test
    void shouldSetAndGetSettings() {
        Tenant tenant = new Tenant();
        Map<String, String> settings = Map.of("key", "value");

        tenant.setSettings(settings);

        assertThat(tenant.getSettings()).isEqualTo(settings);
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        Tenant tenant = new Tenant();
        Instant now = Instant.now();

        tenant.setCreatedAt(now);

        assertThat(tenant.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetUpdatedAt() {
        Tenant tenant = new Tenant();
        Instant now = Instant.now();

        tenant.setUpdatedAt(now);

        assertThat(tenant.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateTenantFromFixtures() {
        Tenant tenant = AdminTestFixtures.createTenant();

        assertThat(tenant).isNotNull();
        assertThat(tenant.getId()).isNotNull();
        assertThat(tenant.getName()).isNotNull();
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.getPlan()).isEqualTo(TenantPlan.PRO);
    }

    @Test
    void shouldCreateTenantWithDifferentPlans() {
        for (TenantPlan plan : TenantPlan.values()) {
            Tenant tenant = AdminTestFixtures.createTenantWithPlan(plan);

            assertThat(tenant.getPlan()).isEqualTo(plan);
            assertThat(tenant.getQuota()).isNotNull();
        }
    }

    @Test
    void shouldCreateTenantWithDifferentStatuses() {
        for (TenantStatus status : TenantStatus.values()) {
            Tenant tenant = AdminTestFixtures.createTenantWithStatus(status);

            assertThat(tenant.getStatus()).isEqualTo(status);
        }
    }
}
