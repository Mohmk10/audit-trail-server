package com.mohmk10.audittrail.admin.domain;

public record ApiKeyCreationResult(
        ApiKey apiKey,
        String plainTextKey
) {
}
