package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionTest {

    @Test
    void shouldHaveSixteenPermissions() {
        assertThat(Permission.values()).hasSize(16);
    }

    @Test
    void shouldContainEventsReadPermission() {
        assertThat(Permission.EVENTS_READ).isNotNull();
        assertThat(Permission.EVENTS_READ.name()).isEqualTo("EVENTS_READ");
    }

    @Test
    void shouldContainEventsWritePermission() {
        assertThat(Permission.EVENTS_WRITE).isNotNull();
        assertThat(Permission.EVENTS_WRITE.name()).isEqualTo("EVENTS_WRITE");
    }

    @Test
    void shouldContainSearchReadPermission() {
        assertThat(Permission.SEARCH_READ).isNotNull();
        assertThat(Permission.SEARCH_READ.name()).isEqualTo("SEARCH_READ");
    }

    @Test
    void shouldContainReportsPermissions() {
        assertThat(Permission.REPORTS_READ).isNotNull();
        assertThat(Permission.REPORTS_WRITE).isNotNull();
    }

    @Test
    void shouldContainRulesPermissions() {
        assertThat(Permission.RULES_READ).isNotNull();
        assertThat(Permission.RULES_WRITE).isNotNull();
    }

    @Test
    void shouldContainAlertsPermissions() {
        assertThat(Permission.ALERTS_READ).isNotNull();
        assertThat(Permission.ALERTS_WRITE).isNotNull();
    }

    @Test
    void shouldContainSourcesPermissions() {
        assertThat(Permission.SOURCES_READ).isNotNull();
        assertThat(Permission.SOURCES_WRITE).isNotNull();
    }

    @Test
    void shouldContainApiKeysPermissions() {
        assertThat(Permission.API_KEYS_READ).isNotNull();
        assertThat(Permission.API_KEYS_WRITE).isNotNull();
    }

    @Test
    void shouldContainUsersPermissions() {
        assertThat(Permission.USERS_READ).isNotNull();
        assertThat(Permission.USERS_WRITE).isNotNull();
    }

    @Test
    void shouldContainTenantSettingsPermission() {
        assertThat(Permission.TENANT_SETTINGS).isNotNull();
        assertThat(Permission.TENANT_SETTINGS.name()).isEqualTo("TENANT_SETTINGS");
    }

    @ParameterizedTest
    @EnumSource(Permission.class)
    void shouldHaveValidName(Permission permission) {
        assertThat(permission.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(Permission.valueOf("EVENTS_READ")).isEqualTo(Permission.EVENTS_READ);
        assertThat(Permission.valueOf("EVENTS_WRITE")).isEqualTo(Permission.EVENTS_WRITE);
        assertThat(Permission.valueOf("TENANT_SETTINGS")).isEqualTo(Permission.TENANT_SETTINGS);
    }
}
