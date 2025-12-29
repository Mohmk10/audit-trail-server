package com.mohmk10.audittrail.search.adapter.in.rest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AggregationRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBeValidWithRequiredFields() {
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", null, null, null
        );

        Set<ConstraintViolation<AggregationRequestDto>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithAllFields() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", "COUNT", from, to
        );

        Set<ConstraintViolation<AggregationRequestDto>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.tenantId()).isEqualTo("tenant-001");
        assertThat(request.groupByField()).isEqualTo("actionType");
        assertThat(request.aggregationType()).isEqualTo("COUNT");
        assertThat(request.fromDate()).isEqualTo(from);
        assertThat(request.toDate()).isEqualTo(to);
    }

    @Test
    void shouldFailValidationWhenTenantIdIsNull() {
        AggregationRequestDto request = new AggregationRequestDto(
                null, "actionType", null, null, null
        );

        Set<ConstraintViolation<AggregationRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldFailValidationWhenTenantIdIsBlank() {
        AggregationRequestDto request = new AggregationRequestDto(
                "  ", "actionType", null, null, null
        );

        Set<ConstraintViolation<AggregationRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldFailValidationWhenGroupByFieldIsNull() {
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", null, null, null, null
        );

        Set<ConstraintViolation<AggregationRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldFailValidationWhenGroupByFieldIsBlank() {
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "  ", null, null, null
        );

        Set<ConstraintViolation<AggregationRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldFailValidationWhenBothRequiredFieldsAreInvalid() {
        AggregationRequestDto request = new AggregationRequestDto(
                null, null, null, null, null
        );

        Set<ConstraintViolation<AggregationRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    @Test
    void shouldDefaultAggregationTypeToCount() {
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", null, null, null
        );

        assertThat(request.aggregationType()).isEqualTo("COUNT");
    }

    @Test
    void shouldDefaultAggregationTypeToCountWhenBlank() {
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", "  ", null, null
        );

        assertThat(request.aggregationType()).isEqualTo("COUNT");
    }

    @Test
    void shouldPreserveCustomAggregationType() {
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", "SUM", null, null
        );

        assertThat(request.aggregationType()).isEqualTo("SUM");
    }

    @Test
    void shouldAcceptNullDates() {
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", null, null, null
        );

        assertThat(request.fromDate()).isNull();
        assertThat(request.toDate()).isNull();
    }

    @Test
    void shouldPreserveDateRange() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "resourceType", "COUNT", from, to
        );

        assertThat(request.fromDate()).isEqualTo(from);
        assertThat(request.toDate()).isEqualTo(to);
    }

    @Test
    void shouldAcceptDifferentGroupByFields() {
        for (String field : new String[]{"actionType", "resourceType", "actorType", "source"}) {
            AggregationRequestDto request = new AggregationRequestDto(
                    "tenant-001", field, null, null, null
            );

            Set<ConstraintViolation<AggregationRequestDto>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
            assertThat(request.groupByField()).isEqualTo(field);
        }
    }

    @Test
    void shouldAcceptOnlyFromDate() {
        Instant from = Instant.now().minusSeconds(3600);
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", null, from, null
        );

        assertThat(request.fromDate()).isEqualTo(from);
        assertThat(request.toDate()).isNull();
    }

    @Test
    void shouldAcceptOnlyToDate() {
        Instant to = Instant.now();
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", null, null, to
        );

        assertThat(request.fromDate()).isNull();
        assertThat(request.toDate()).isEqualTo(to);
    }
}
