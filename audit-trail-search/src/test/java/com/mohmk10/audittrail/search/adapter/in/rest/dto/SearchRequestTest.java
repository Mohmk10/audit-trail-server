package com.mohmk10.audittrail.search.adapter.in.rest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SearchRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBeValidWithRequiredFields() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, 0, 20, null, null
        );

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithAllFields() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        SearchRequest request = new SearchRequest(
                "tenant-001", "actor-123", "USER", "CREATE", "DOCS",
                "res-123", "DOCUMENT", "annual report", from, to,
                List.of("env:prod", "region:us"), 0, 50, "timestamp", "desc"
        );

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.tenantId()).isEqualTo("tenant-001");
        assertThat(request.actorId()).isEqualTo("actor-123");
        assertThat(request.actorType()).isEqualTo("USER");
        assertThat(request.actionType()).isEqualTo("CREATE");
        assertThat(request.query()).isEqualTo("annual report");
    }

    @Test
    void shouldFailValidationWhenTenantIdIsNull() {
        SearchRequest request = new SearchRequest(
                null, null, null, null, null, null, null,
                null, null, null, null, 0, 20, null, null
        );

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldFailValidationWhenTenantIdIsBlank() {
        SearchRequest request = new SearchRequest(
                "  ", null, null, null, null, null, null,
                null, null, null, null, 0, 20, null, null
        );

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldNormalizeNegativePage() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, -1, 20, null, null
        );

        assertThat(request.page()).isEqualTo(0);
    }

    @Test
    void shouldNormalizeZeroSize() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, 0, 0, null, null
        );

        assertThat(request.size()).isEqualTo(20);
    }

    @Test
    void shouldNormalizeNegativeSize() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, 0, -10, null, null
        );

        assertThat(request.size()).isEqualTo(20);
    }

    @Test
    void shouldLimitMaxSize() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, 0, 200, null, null
        );

        assertThat(request.size()).isEqualTo(100);
    }

    @Test
    void shouldAllowMaxSize() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, 0, 100, null, null
        );

        assertThat(request.size()).isEqualTo(100);
    }

    @Test
    void shouldPreserveDateRange() {
        Instant from = Instant.parse("2024-06-01T00:00:00Z");
        Instant to = Instant.parse("2024-06-30T23:59:59Z");
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, from, to, null, 0, 20, null, null
        );

        assertThat(request.fromDate()).isEqualTo(from);
        assertThat(request.toDate()).isEqualTo(to);
    }

    @Test
    void shouldPreserveTags() {
        List<String> tags = List.of("env:prod", "region:us-east-1", "team:backend");
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, tags, 0, 20, null, null
        );

        assertThat(request.tags()).hasSize(3);
        assertThat(request.tags()).contains("env:prod");
    }

    @Test
    void shouldPreserveSortOptions() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, 0, 20, "timestamp", "asc"
        );

        assertThat(request.sortBy()).isEqualTo("timestamp");
        assertThat(request.sortOrder()).isEqualTo("asc");
    }

    @Test
    void shouldAcceptNullOptionalFields() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, 0, 20, null, null
        );

        assertThat(request.actorId()).isNull();
        assertThat(request.actionType()).isNull();
        assertThat(request.query()).isNull();
        assertThat(request.fromDate()).isNull();
        assertThat(request.toDate()).isNull();
        assertThat(request.tags()).isNull();
    }

    @Test
    void shouldPreserveResourceFilters() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, "res-123", "DOCUMENT",
                null, null, null, null, 0, 20, null, null
        );

        assertThat(request.resourceId()).isEqualTo("res-123");
        assertThat(request.resourceType()).isEqualTo("DOCUMENT");
    }

    @Test
    void shouldPreserveActionFilters() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, "CREATE", "DOCS", null, null,
                null, null, null, null, 0, 20, null, null
        );

        assertThat(request.actionType()).isEqualTo("CREATE");
        assertThat(request.actionCategory()).isEqualTo("DOCS");
    }
}
