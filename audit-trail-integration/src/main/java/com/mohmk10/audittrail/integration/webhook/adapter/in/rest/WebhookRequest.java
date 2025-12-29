package com.mohmk10.audittrail.integration.webhook.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.Map;
import java.util.Set;

public record WebhookRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    @NotBlank(message = "URL is required")
    @URL(message = "URL must be valid")
    String url,

    @NotEmpty(message = "At least one event must be subscribed")
    Set<String> events,

    Map<String, String> headers,

    Integer maxRetries
) {
    public WebhookRequest {
        if (headers == null) {
            headers = Map.of();
        }
        if (maxRetries == null) {
            maxRetries = 5;
        }
    }
}
