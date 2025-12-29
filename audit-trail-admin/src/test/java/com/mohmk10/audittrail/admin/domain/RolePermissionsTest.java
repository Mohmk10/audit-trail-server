package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RolePermissionsTest {

    @Test
    void shouldReturnAllPermissionsForAdmin() {
        Set<Permission> permissions = RolePermissions.getPermissions(Role.ADMIN);

        assertThat(permissions).containsExactlyInAnyOrder(Permission.values());
    }

    @Test
    void shouldReturnLimitedPermissionsForAuditor() {
        Set<Permission> permissions = RolePermissions.getPermissions(Role.AUDITOR);

        assertThat(permissions).contains(
                Permission.EVENTS_READ,
                Permission.SEARCH_READ,
                Permission.REPORTS_READ,
                Permission.REPORTS_WRITE,
                Permission.RULES_READ,
                Permission.ALERTS_READ,
                Permission.ALERTS_WRITE
        );
        assertThat(permissions).doesNotContain(
                Permission.EVENTS_WRITE,
                Permission.SOURCES_WRITE,
                Permission.USERS_WRITE,
                Permission.TENANT_SETTINGS
        );
    }

    @Test
    void shouldReturnReadOnlyPermissionsForViewer() {
        Set<Permission> permissions = RolePermissions.getPermissions(Role.VIEWER);

        assertThat(permissions).contains(
                Permission.EVENTS_READ,
                Permission.SEARCH_READ,
                Permission.REPORTS_READ,
                Permission.ALERTS_READ
        );
        assertThat(permissions).doesNotContain(
                Permission.EVENTS_WRITE,
                Permission.REPORTS_WRITE,
                Permission.RULES_WRITE,
                Permission.ALERTS_WRITE,
                Permission.SOURCES_WRITE,
                Permission.USERS_WRITE
        );
    }

    @Test
    void shouldReturnTrueWhenAdminHasPermission() {
        for (Permission permission : Permission.values()) {
            assertThat(RolePermissions.hasPermission(Role.ADMIN, permission))
                    .as("Admin should have permission: %s", permission)
                    .isTrue();
        }
    }

    @Test
    void shouldReturnTrueWhenAuditorHasEventsReadPermission() {
        assertThat(RolePermissions.hasPermission(Role.AUDITOR, Permission.EVENTS_READ)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAuditorLacksEventsWritePermission() {
        assertThat(RolePermissions.hasPermission(Role.AUDITOR, Permission.EVENTS_WRITE)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAuditorHasReportsWritePermission() {
        assertThat(RolePermissions.hasPermission(Role.AUDITOR, Permission.REPORTS_WRITE)).isTrue();
    }

    @Test
    void shouldReturnTrueWhenAuditorHasAlertsWritePermission() {
        assertThat(RolePermissions.hasPermission(Role.AUDITOR, Permission.ALERTS_WRITE)).isTrue();
    }

    @Test
    void shouldReturnTrueWhenViewerHasEventsReadPermission() {
        assertThat(RolePermissions.hasPermission(Role.VIEWER, Permission.EVENTS_READ)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenViewerLacksWritePermissions() {
        assertThat(RolePermissions.hasPermission(Role.VIEWER, Permission.EVENTS_WRITE)).isFalse();
        assertThat(RolePermissions.hasPermission(Role.VIEWER, Permission.REPORTS_WRITE)).isFalse();
        assertThat(RolePermissions.hasPermission(Role.VIEWER, Permission.ALERTS_WRITE)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenViewerLacksTenantSettingsPermission() {
        assertThat(RolePermissions.hasPermission(Role.VIEWER, Permission.TENANT_SETTINGS)).isFalse();
    }

    @Test
    void shouldThrowExceptionForNullRoleInGetPermissions() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () ->
            RolePermissions.getPermissions(null)
        );
    }

    @Test
    void shouldThrowExceptionForNullRoleInHasPermission() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () ->
            RolePermissions.hasPermission(null, Permission.EVENTS_READ)
        );
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void shouldReturnNonEmptyPermissionsForAllRoles(Role role) {
        Set<Permission> permissions = RolePermissions.getPermissions(role);

        assertThat(permissions).isNotEmpty();
    }

    @Test
    void shouldHaveMorePermissionsThanViewerForAuditor() {
        Set<Permission> auditorPermissions = RolePermissions.getPermissions(Role.AUDITOR);
        Set<Permission> viewerPermissions = RolePermissions.getPermissions(Role.VIEWER);

        assertThat(auditorPermissions.size()).isGreaterThan(viewerPermissions.size());
    }

    @Test
    void shouldHaveMorePermissionsThanAuditorForAdmin() {
        Set<Permission> adminPermissions = RolePermissions.getPermissions(Role.ADMIN);
        Set<Permission> auditorPermissions = RolePermissions.getPermissions(Role.AUDITOR);

        assertThat(adminPermissions.size()).isGreaterThan(auditorPermissions.size());
    }
}
