package com.mohmk10.audittrail.core.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Action(
        @NotNull ActionType type,
        String description,
        String category
) {
    public enum ActionType {
        CREATE, READ, UPDATE, DELETE, APPROVE, REJECT, LOGIN, LOGOUT, EXPORT, IMPORT, ARCHIVE, RESTORE
    }
}
