package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void shouldHaveThreeRoles() {
        assertThat(Role.values()).hasSize(3);
    }

    @Test
    void shouldContainAdminRole() {
        assertThat(Role.ADMIN).isNotNull();
        assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
    }

    @Test
    void shouldContainAuditorRole() {
        assertThat(Role.AUDITOR).isNotNull();
        assertThat(Role.AUDITOR.name()).isEqualTo("AUDITOR");
    }

    @Test
    void shouldContainViewerRole() {
        assertThat(Role.VIEWER).isNotNull();
        assertThat(Role.VIEWER.name()).isEqualTo("VIEWER");
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void shouldHaveValidName(Role role) {
        assertThat(role.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
        assertThat(Role.valueOf("AUDITOR")).isEqualTo(Role.AUDITOR);
        assertThat(Role.valueOf("VIEWER")).isEqualTo(Role.VIEWER);
    }

    @Test
    void shouldHaveCorrectOrdinal() {
        assertThat(Role.ADMIN.ordinal()).isEqualTo(0);
        assertThat(Role.AUDITOR.ordinal()).isEqualTo(1);
        assertThat(Role.VIEWER.ordinal()).isEqualTo(2);
    }
}
