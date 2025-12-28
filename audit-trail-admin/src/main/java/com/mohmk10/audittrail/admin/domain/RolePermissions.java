package com.mohmk10.audittrail.admin.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class RolePermissions {

    private static final Map<Role, Set<Permission>> PERMISSIONS = Map.of(
            Role.ADMIN, EnumSet.allOf(Permission.class),
            Role.AUDITOR, EnumSet.of(
                    Permission.EVENTS_READ,
                    Permission.SEARCH_READ,
                    Permission.REPORTS_READ,
                    Permission.REPORTS_WRITE,
                    Permission.RULES_READ,
                    Permission.ALERTS_READ,
                    Permission.ALERTS_WRITE
            ),
            Role.VIEWER, EnumSet.of(
                    Permission.EVENTS_READ,
                    Permission.SEARCH_READ,
                    Permission.REPORTS_READ,
                    Permission.ALERTS_READ
            )
    );

    private RolePermissions() {
    }

    public static boolean hasPermission(Role role, Permission permission) {
        Set<Permission> permissions = PERMISSIONS.get(role);
        return permissions != null && permissions.contains(permission);
    }

    public static Set<Permission> getPermissions(Role role) {
        return PERMISSIONS.getOrDefault(role, Set.of());
    }
}
