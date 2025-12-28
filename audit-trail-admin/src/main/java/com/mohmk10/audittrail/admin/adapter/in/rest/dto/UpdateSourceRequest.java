package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.SourceType;

import java.util.Map;

public record UpdateSourceRequest(
        String name,
        String description,
        SourceType type,
        Map<String, String> config
) {
}
