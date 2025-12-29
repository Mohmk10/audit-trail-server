package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantQuotaTest {

    @Test
    void shouldCreateQuotaWithConstructor() {
        TenantQuota quota = new TenantQuota(10000L, 300000L, 10, 20, 50, 90);

        assertThat(quota.getMaxEventsPerDay()).isEqualTo(10000L);
        assertThat(quota.getMaxEventsPerMonth()).isEqualTo(300000L);
        assertThat(quota.getMaxSources()).isEqualTo(10);
        assertThat(quota.getMaxApiKeys()).isEqualTo(20);
        assertThat(quota.getMaxUsers()).isEqualTo(50);
        assertThat(quota.getRetentionDays()).isEqualTo(90);
    }

    @Test
    void shouldCreateEmptyQuota() {
        TenantQuota quota = new TenantQuota();

        assertThat(quota.getMaxEventsPerDay()).isZero();
        assertThat(quota.getMaxEventsPerMonth()).isZero();
        assertThat(quota.getMaxSources()).isZero();
        assertThat(quota.getMaxApiKeys()).isZero();
        assertThat(quota.getMaxUsers()).isZero();
        assertThat(quota.getRetentionDays()).isZero();
    }

    @Test
    void shouldSetAndGetMaxEventsPerDay() {
        TenantQuota quota = new TenantQuota();

        quota.setMaxEventsPerDay(5000L);

        assertThat(quota.getMaxEventsPerDay()).isEqualTo(5000L);
    }

    @Test
    void shouldSetAndGetMaxEventsPerMonth() {
        TenantQuota quota = new TenantQuota();

        quota.setMaxEventsPerMonth(150000L);

        assertThat(quota.getMaxEventsPerMonth()).isEqualTo(150000L);
    }

    @Test
    void shouldSetAndGetMaxSources() {
        TenantQuota quota = new TenantQuota();

        quota.setMaxSources(25);

        assertThat(quota.getMaxSources()).isEqualTo(25);
    }

    @Test
    void shouldSetAndGetMaxApiKeys() {
        TenantQuota quota = new TenantQuota();

        quota.setMaxApiKeys(100);

        assertThat(quota.getMaxApiKeys()).isEqualTo(100);
    }

    @Test
    void shouldSetAndGetMaxUsers() {
        TenantQuota quota = new TenantQuota();

        quota.setMaxUsers(200);

        assertThat(quota.getMaxUsers()).isEqualTo(200);
    }

    @Test
    void shouldSetAndGetRetentionDays() {
        TenantQuota quota = new TenantQuota();

        quota.setRetentionDays(365);

        assertThat(quota.getRetentionDays()).isEqualTo(365);
    }

    @Test
    void shouldCreateQuotaFromFixtures() {
        TenantQuota quota = AdminTestFixtures.createTenantQuota();

        assertThat(quota.getMaxEventsPerDay()).isPositive();
        assertThat(quota.getMaxEventsPerMonth()).isPositive();
        assertThat(quota.getMaxSources()).isPositive();
        assertThat(quota.getMaxApiKeys()).isPositive();
        assertThat(quota.getMaxUsers()).isPositive();
        assertThat(quota.getRetentionDays()).isPositive();
    }

    @Test
    void shouldCreateDifferentQuotasForDifferentPlans() {
        TenantQuota freeQuota = AdminTestFixtures.createQuotaForPlan(TenantPlan.FREE);
        TenantQuota enterpriseQuota = AdminTestFixtures.createQuotaForPlan(TenantPlan.ENTERPRISE);

        assertThat(freeQuota.getMaxEventsPerDay()).isLessThan(enterpriseQuota.getMaxEventsPerDay());
        assertThat(freeQuota.getMaxEventsPerMonth()).isLessThan(enterpriseQuota.getMaxEventsPerMonth());
        assertThat(freeQuota.getMaxSources()).isLessThan(enterpriseQuota.getMaxSources());
        assertThat(freeQuota.getMaxApiKeys()).isLessThan(enterpriseQuota.getMaxApiKeys());
        assertThat(freeQuota.getMaxUsers()).isLessThan(enterpriseQuota.getMaxUsers());
        assertThat(freeQuota.getRetentionDays()).isLessThan(enterpriseQuota.getRetentionDays());
    }

    @Test
    void shouldCreateQuotaForAllPlans() {
        for (TenantPlan plan : TenantPlan.values()) {
            TenantQuota quota = AdminTestFixtures.createQuotaForPlan(plan);

            assertThat(quota).isNotNull();
            assertThat(quota.getMaxEventsPerDay()).isPositive();
        }
    }
}
