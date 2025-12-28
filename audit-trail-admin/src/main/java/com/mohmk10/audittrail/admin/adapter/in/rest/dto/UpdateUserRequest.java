package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        @NotNull(message = "Role is required")
        Role role
) {
}
