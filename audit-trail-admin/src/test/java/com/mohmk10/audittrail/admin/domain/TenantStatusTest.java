package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class TenantStatusTest {

    @Test
    void shouldHaveFourStatusTypes() {
        assertThat(TenantStatus.values()).hasSize(4);
    }

    @Test
    void shouldContainActiveStatus() {
        assertThat(TenantStatus.ACTIVE).isNotNull();
        assertThat(TenantStatus.ACTIVE.name()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldContainSuspendedStatus() {
        assertThat(TenantStatus.SUSPENDED).isNotNull();
        assertThat(TenantStatus.SUSPENDED.name()).isEqualTo("SUSPENDED");
    }

    @Test
    void shouldContainPendingStatus() {
        assertThat(TenantStatus.PENDING).isNotNull();
        assertThat(TenantStatus.PENDING.name()).isEqualTo("PENDING");
    }

    @Test
    void shouldContainDeletedStatus() {
        assertThat(TenantStatus.DELETED).isNotNull();
        assertThat(TenantStatus.DELETED.name()).isEqualTo("DELETED");
    }

    @ParameterizedTest
    @EnumSource(TenantStatus.class)
    void shouldHaveValidName(TenantStatus status) {
        assertThat(status.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(TenantStatus.valueOf("ACTIVE")).isEqualTo(TenantStatus.ACTIVE);
        assertThat(TenantStatus.valueOf("SUSPENDED")).isEqualTo(TenantStatus.SUSPENDED);
        assertThat(TenantStatus.valueOf("PENDING")).isEqualTo(TenantStatus.PENDING);
        assertThat(TenantStatus.valueOf("DELETED")).isEqualTo(TenantStatus.DELETED);
    }
}
