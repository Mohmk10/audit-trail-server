package com.mohmk10.audittrail.ingestion.adapter.in.rest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ActionRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBeValidWithRequiredFields() {
        ActionRequest request = new ActionRequest("CREATE", null, null);

        Set<ConstraintViolation<ActionRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithAllFields() {
        ActionRequest request = new ActionRequest("CREATE", "Created a new document", "DOCUMENT");

        Set<ConstraintViolation<ActionRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.type()).isEqualTo("CREATE");
        assertThat(request.description()).isEqualTo("Created a new document");
        assertThat(request.category()).isEqualTo("DOCUMENT");
    }

    @Test
    void shouldFailValidationWhenTypeIsNull() {
        ActionRequest request = new ActionRequest(null, "Description", "CATEGORY");

        Set<ConstraintViolation<ActionRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Action type is required");
    }

    @Test
    void shouldFailValidationWhenTypeIsBlank() {
        ActionRequest request = new ActionRequest("", "Description", "CATEGORY");

        Set<ConstraintViolation<ActionRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Action type is required");
    }

    @Test
    void shouldFailValidationWhenTypeIsWhitespace() {
        ActionRequest request = new ActionRequest("   ", "Description", "CATEGORY");

        Set<ConstraintViolation<ActionRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldAcceptAllActionTypes() {
        for (String type : new String[]{"CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "LOGOUT"}) {
            ActionRequest request = new ActionRequest(type, null, null);
            Set<ConstraintViolation<ActionRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    void shouldAcceptNullDescription() {
        ActionRequest request = new ActionRequest("UPDATE", null, null);

        Set<ConstraintViolation<ActionRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.description()).isNull();
    }

    @Test
    void shouldAcceptNullCategory() {
        ActionRequest request = new ActionRequest("DELETE", "Deleted resource", null);

        Set<ConstraintViolation<ActionRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.category()).isNull();
    }

    @Test
    void shouldPreserveLongDescription() {
        String longDescription = "This is a very long description that describes " +
                "the action in great detail including all the context and reasoning behind it";
        ActionRequest request = new ActionRequest("CREATE", longDescription, null);

        assertThat(request.description()).isEqualTo(longDescription);
    }

    @Test
    void shouldAcceptCustomActionTypes() {
        ActionRequest request = new ActionRequest("CUSTOM_ACTION", null, null);

        Set<ConstraintViolation<ActionRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
