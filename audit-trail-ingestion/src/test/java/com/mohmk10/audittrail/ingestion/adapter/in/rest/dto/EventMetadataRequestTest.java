package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EventMetadataRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBeValidWithRequiredFields() {
        EventMetadataRequest request = new EventMetadataRequest("web-app", "tenant-001", null, null, null, null);

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithAllFields() {
        Map<String, String> tags = Map.of("env", "production", "version", "1.0");
        Map<String, Object> extra = Map.of("customField", "customValue", "count", 42);
        EventMetadataRequest request = new EventMetadataRequest(
                "web-app",
                "tenant-001",
                "corr-123",
                "session-abc",
                tags,
                extra
        );

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.source()).isEqualTo("web-app");
        assertThat(request.tenantId()).isEqualTo("tenant-001");
        assertThat(request.correlationId()).isEqualTo("corr-123");
        assertThat(request.sessionId()).isEqualTo("session-abc");
        assertThat(request.tags()).containsEntry("env", "production");
        assertThat(request.extra()).containsEntry("count", 42);
    }

    @Test
    void shouldFailValidationWhenSourceIsNull() {
        EventMetadataRequest request = new EventMetadataRequest(null, "tenant-001", null, null, null, null);

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Source is required");
    }

    @Test
    void shouldFailValidationWhenSourceIsBlank() {
        EventMetadataRequest request = new EventMetadataRequest("  ", "tenant-001", null, null, null, null);

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Source is required");
    }

    @Test
    void shouldFailValidationWhenTenantIdIsNull() {
        EventMetadataRequest request = new EventMetadataRequest("web-app", null, null, null, null, null);

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Tenant ID is required");
    }

    @Test
    void shouldFailValidationWhenTenantIdIsBlank() {
        EventMetadataRequest request = new EventMetadataRequest("web-app", "", null, null, null, null);

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Tenant ID is required");
    }

    @Test
    void shouldFailValidationWhenBothSourceAndTenantIdAreInvalid() {
        EventMetadataRequest request = new EventMetadataRequest(null, null, null, null, null, null);

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    @Test
    void shouldAcceptNullOptionalFields() {
        EventMetadataRequest request = new EventMetadataRequest("web-app", "tenant-001", null, null, null, null);

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.correlationId()).isNull();
        assertThat(request.sessionId()).isNull();
        assertThat(request.tags()).isNull();
        assertThat(request.extra()).isNull();
    }

    @Test
    void shouldAcceptEmptyTags() {
        EventMetadataRequest request = new EventMetadataRequest("web-app", "tenant-001", null, null, Map.of(), null);

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.tags()).isEmpty();
    }

    @Test
    void shouldAcceptEmptyExtra() {
        EventMetadataRequest request = new EventMetadataRequest("web-app", "tenant-001", null, null, null, Map.of());

        Set<ConstraintViolation<EventMetadataRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.extra()).isEmpty();
    }

    @Test
    void shouldPreserveCorrelationId() {
        String correlationId = "12345678-1234-1234-1234-123456789abc";
        EventMetadataRequest request = new EventMetadataRequest("web-app", "tenant-001", correlationId, null, null, null);

        assertThat(request.correlationId()).isEqualTo(correlationId);
    }

    @Test
    void shouldPreserveSessionId() {
        String sessionId = "session-abc-123";
        EventMetadataRequest request = new EventMetadataRequest("web-app", "tenant-001", null, sessionId, null, null);

        assertThat(request.sessionId()).isEqualTo(sessionId);
    }

    @Test
    void shouldAcceptMultipleTags() {
        Map<String, String> tags = Map.of(
                "env", "production",
                "version", "1.0",
                "region", "us-east-1",
                "team", "backend"
        );
        EventMetadataRequest request = new EventMetadataRequest("web-app", "tenant-001", null, null, tags, null);

        assertThat(request.tags()).hasSize(4);
    }

    @Test
    void shouldAcceptComplexExtraData() {
        Map<String, Object> extra = Map.of(
                "stringField", "value",
                "intField", 42,
                "boolField", true,
                "listField", java.util.List.of("a", "b", "c")
        );
        EventMetadataRequest request = new EventMetadataRequest("web-app", "tenant-001", null, null, null, extra);

        assertThat(request.extra()).containsKeys("stringField", "intField", "boolField", "listField");
    }
}
