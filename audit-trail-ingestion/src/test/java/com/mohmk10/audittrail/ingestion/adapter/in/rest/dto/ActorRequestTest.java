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

class ActorRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBeValidWithRequiredFields() {
        ActorRequest request = new ActorRequest("actor-123", "USER", null, null, null, null);

        Set<ConstraintViolation<ActorRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithAllFields() {
        Map<String, String> attributes = Map.of("role", "admin", "department", "IT");
        ActorRequest request = new ActorRequest(
                "actor-123",
                "USER",
                "John Doe",
                "192.168.1.1",
                "Mozilla/5.0",
                attributes
        );

        Set<ConstraintViolation<ActorRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.id()).isEqualTo("actor-123");
        assertThat(request.type()).isEqualTo("USER");
        assertThat(request.name()).isEqualTo("John Doe");
        assertThat(request.ip()).isEqualTo("192.168.1.1");
        assertThat(request.userAgent()).isEqualTo("Mozilla/5.0");
        assertThat(request.attributes()).containsEntry("role", "admin");
    }

    @Test
    void shouldFailValidationWhenIdIsNull() {
        ActorRequest request = new ActorRequest(null, "USER", null, null, null, null);

        Set<ConstraintViolation<ActorRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Actor ID is required");
    }

    @Test
    void shouldFailValidationWhenIdIsBlank() {
        ActorRequest request = new ActorRequest("  ", "USER", null, null, null, null);

        Set<ConstraintViolation<ActorRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Actor ID is required");
    }

    @Test
    void shouldFailValidationWhenTypeIsNull() {
        ActorRequest request = new ActorRequest("actor-123", null, null, null, null, null);

        Set<ConstraintViolation<ActorRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Actor type is required");
    }

    @Test
    void shouldFailValidationWhenTypeIsBlank() {
        ActorRequest request = new ActorRequest("actor-123", "", null, null, null, null);

        Set<ConstraintViolation<ActorRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Actor type is required");
    }

    @Test
    void shouldFailValidationWhenBothIdAndTypeAreInvalid() {
        ActorRequest request = new ActorRequest(null, null, null, null, null, null);

        Set<ConstraintViolation<ActorRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    @Test
    void shouldAcceptDifferentActorTypes() {
        for (String type : new String[]{"USER", "SYSTEM", "SERVICE", "API"}) {
            ActorRequest request = new ActorRequest("actor-123", type, null, null, null, null);
            Set<ConstraintViolation<ActorRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    void shouldAcceptEmptyAttributes() {
        ActorRequest request = new ActorRequest("actor-123", "USER", null, null, null, Map.of());

        Set<ConstraintViolation<ActorRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.attributes()).isEmpty();
    }

    @Test
    void shouldPreserveIpAddress() {
        ActorRequest request = new ActorRequest("actor-123", "USER", null, "10.0.0.1", null, null);

        assertThat(request.ip()).isEqualTo("10.0.0.1");
    }

    @Test
    void shouldPreserveUserAgent() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0";
        ActorRequest request = new ActorRequest("actor-123", "USER", null, null, userAgent, null);

        assertThat(request.userAgent()).isEqualTo(userAgent);
    }
}
