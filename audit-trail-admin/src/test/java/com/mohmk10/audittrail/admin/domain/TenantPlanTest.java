package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class TenantPlanTest {

    @Test
    void shouldHaveFourPlanTypes() {
        assertThat(TenantPlan.values()).hasSize(4);
    }

    @Test
    void shouldContainFreePlan() {
        assertThat(TenantPlan.FREE).isNotNull();
        assertThat(TenantPlan.FREE.name()).isEqualTo("FREE");
    }

    @Test
    void shouldContainStarterPlan() {
        assertThat(TenantPlan.STARTER).isNotNull();
        assertThat(TenantPlan.STARTER.name()).isEqualTo("STARTER");
    }

    @Test
    void shouldContainProPlan() {
        assertThat(TenantPlan.PRO).isNotNull();
        assertThat(TenantPlan.PRO.name()).isEqualTo("PRO");
    }

    @Test
    void shouldContainEnterprisePlan() {
        assertThat(TenantPlan.ENTERPRISE).isNotNull();
        assertThat(TenantPlan.ENTERPRISE.name()).isEqualTo("ENTERPRISE");
    }

    @ParameterizedTest
    @EnumSource(TenantPlan.class)
    void shouldHaveValidName(TenantPlan plan) {
        assertThat(plan.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(TenantPlan.valueOf("FREE")).isEqualTo(TenantPlan.FREE);
        assertThat(TenantPlan.valueOf("STARTER")).isEqualTo(TenantPlan.STARTER);
        assertThat(TenantPlan.valueOf("PRO")).isEqualTo(TenantPlan.PRO);
        assertThat(TenantPlan.valueOf("ENTERPRISE")).isEqualTo(TenantPlan.ENTERPRISE);
    }

    @Test
    void shouldHaveCorrectOrdinal() {
        assertThat(TenantPlan.FREE.ordinal()).isEqualTo(0);
        assertThat(TenantPlan.STARTER.ordinal()).isEqualTo(1);
        assertThat(TenantPlan.PRO.ordinal()).isEqualTo(2);
        assertThat(TenantPlan.ENTERPRISE.ordinal()).isEqualTo(3);
    }
}
