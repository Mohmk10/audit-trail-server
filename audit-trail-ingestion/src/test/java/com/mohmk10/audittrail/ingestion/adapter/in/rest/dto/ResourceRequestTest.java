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

class ResourceRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBeValidWithRequiredFields() {
        ResourceRequest request = new ResourceRequest("res-123", "DOCUMENT", null, null, null);

        Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithAllFields() {
        Map<String, Object> before = Map.of("status", "draft", "version", 1);
        Map<String, Object> after = Map.of("status", "published", "version", 2);
        ResourceRequest request = new ResourceRequest(
                "res-123",
                "DOCUMENT",
                "Annual Report",
                before,
                after
        );

        Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.id()).isEqualTo("res-123");
        assertThat(request.type()).isEqualTo("DOCUMENT");
        assertThat(request.name()).isEqualTo("Annual Report");
        assertThat(request.before()).containsEntry("status", "draft");
        assertThat(request.after()).containsEntry("status", "published");
    }

    @Test
    void shouldFailValidationWhenIdIsNull() {
        ResourceRequest request = new ResourceRequest(null, "DOCUMENT", null, null, null);

        Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Resource ID is required");
    }

    @Test
    void shouldFailValidationWhenIdIsBlank() {
        ResourceRequest request = new ResourceRequest("  ", "DOCUMENT", null, null, null);

        Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Resource ID is required");
    }

    @Test
    void shouldFailValidationWhenTypeIsNull() {
        ResourceRequest request = new ResourceRequest("res-123", null, null, null, null);

        Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Resource type is required");
    }

    @Test
    void shouldFailValidationWhenTypeIsBlank() {
        ResourceRequest request = new ResourceRequest("res-123", "", null, null, null);

        Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Resource type is required");
    }

    @Test
    void shouldFailValidationWhenBothIdAndTypeAreInvalid() {
        ResourceRequest request = new ResourceRequest(null, null, null, null, null);

        Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    @Test
    void shouldAcceptAllResourceTypes() {
        for (String type : new String[]{"DOCUMENT", "USER", "TRANSACTION", "CONFIG", "FILE", "API"}) {
            ResourceRequest request = new ResourceRequest("res-123", type, null, null, null);
            Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    void shouldAcceptNullName() {
        ResourceRequest request = new ResourceRequest("res-123", "FILE", null, null, null);

        Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.name()).isNull();
    }

    @Test
    void shouldAcceptEmptyBeforeAndAfter() {
        ResourceRequest request = new ResourceRequest("res-123", "DOCUMENT", "Test", Map.of(), Map.of());

        Set<ConstraintViolation<ResourceRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.before()).isEmpty();
        assertThat(request.after()).isEmpty();
    }

    @Test
    void shouldPreserveNestedObjectsInBeforeAndAfter() {
        Map<String, Object> before = Map.of(
                "status", "draft",
                "metadata", Map.of("author", "John"),
                "tags", java.util.List.of("tag1", "tag2")
        );
        Map<String, Object> after = Map.of(
                "status", "published",
                "metadata", Map.of("author", "John", "reviewer", "Jane"),
                "tags", java.util.List.of("tag1", "tag2", "tag3")
        );
        ResourceRequest request = new ResourceRequest("res-123", "DOCUMENT", "Test", before, after);

        assertThat(request.before()).containsKey("metadata");
        assertThat(request.after()).containsKey("tags");
    }

    @Test
    void shouldAcceptOnlyBefore() {
        Map<String, Object> before = Map.of("field", "value");
        ResourceRequest request = new ResourceRequest("res-123", "DOCUMENT", null, before, null);

        assertThat(request.before()).isNotNull();
        assertThat(request.after()).isNull();
    }

    @Test
    void shouldAcceptOnlyAfter() {
        Map<String, Object> after = Map.of("field", "value");
        ResourceRequest request = new ResourceRequest("res-123", "DOCUMENT", null, null, after);

        assertThat(request.before()).isNull();
        assertThat(request.after()).isNotNull();
    }
}
