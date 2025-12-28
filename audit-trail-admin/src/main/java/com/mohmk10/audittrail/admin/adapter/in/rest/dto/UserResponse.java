package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.Permission;
import com.mohmk10.audittrail.admin.domain.Role;
import com.mohmk10.audittrail.admin.domain.RolePermissions;
import com.mohmk10.audittrail.admin.domain.User;
import com.mohmk10.audittrail.admin.domain.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String tenantId,
        String email,
        String firstName,
        String lastName,
        String fullName,
        Role role,
        Set<Permission> permissions,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant lastLoginAt,
        String lastLoginIp
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getRole(),
                RolePermissions.getPermissions(user.getRole()),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                user.getLastLoginIp()
        );
    }
}
