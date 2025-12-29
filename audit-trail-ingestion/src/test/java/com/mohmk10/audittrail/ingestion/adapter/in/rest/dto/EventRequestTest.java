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

class EventRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ActorRequest validActor() {
        return new ActorRequest("actor-123", "USER", "John Doe", null, null, null);
    }

    private ActionRequest validAction() {
        return new ActionRequest("CREATE", "Created a document", null);
    }

    private ResourceRequest validResource() {
        return new ResourceRequest("res-123", "DOCUMENT", "Annual Report", null, null);
    }

    private EventMetadataRequest validMetadata() {
        return new EventMetadataRequest("web-app", "tenant-001", null, null, null, null);
    }

    @Test
    void shouldBeValidWithAllRequiredFields() {
        EventRequest request = new EventRequest(validActor(), validAction(), validResource(), null);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithAllFields() {
        EventRequest request = new EventRequest(validActor(), validAction(), validResource(), validMetadata());

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.actor()).isNotNull();
        assertThat(request.action()).isNotNull();
        assertThat(request.resource()).isNotNull();
        assertThat(request.metadata()).isNotNull();
    }

    @Test
    void shouldFailValidationWhenActorIsNull() {
        EventRequest request = new EventRequest(null, validAction(), validResource(), null);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Actor is required");
    }

    @Test
    void shouldFailValidationWhenActionIsNull() {
        EventRequest request = new EventRequest(validActor(), null, validResource(), null);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Action is required");
    }

    @Test
    void shouldFailValidationWhenResourceIsNull() {
        EventRequest request = new EventRequest(validActor(), validAction(), null, null);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Resource is required");
    }

    @Test
    void shouldFailValidationWhenAllRequiredFieldsAreNull() {
        EventRequest request = new EventRequest(null, null, null, null);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(3);
    }

    @Test
    void shouldCascadeValidationToNestedActor() {
        ActorRequest invalidActor = new ActorRequest(null, "USER", null, null, null, null);
        EventRequest request = new EventRequest(invalidActor, validAction(), validResource(), null);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).startsWith("actor");
    }

    @Test
    void shouldCascadeValidationToNestedAction() {
        ActionRequest invalidAction = new ActionRequest(null, null, null);
        EventRequest request = new EventRequest(validActor(), invalidAction, validResource(), null);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).startsWith("action");
    }

    @Test
    void shouldCascadeValidationToNestedResource() {
        ResourceRequest invalidResource = new ResourceRequest(null, "DOCUMENT", null, null, null);
        EventRequest request = new EventRequest(validActor(), validAction(), invalidResource, null);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).startsWith("resource");
    }

    @Test
    void shouldCascadeValidationToNestedMetadata() {
        EventMetadataRequest invalidMetadata = new EventMetadataRequest(null, "tenant-001", null, null, null, null);
        EventRequest request = new EventRequest(validActor(), validAction(), validResource(), invalidMetadata);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).startsWith("metadata");
    }

    @Test
    void shouldAcceptNullMetadata() {
        EventRequest request = new EventRequest(validActor(), validAction(), validResource(), null);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.metadata()).isNull();
    }

    @Test
    void shouldPreserveAllNestedData() {
        Map<String, String> actorAttributes = Map.of("role", "admin");
        ActorRequest actor = new ActorRequest("actor-123", "USER", "John", "192.168.1.1", "Mozilla/5.0", actorAttributes);
        ActionRequest action = new ActionRequest("UPDATE", "Updated document", "DOCS");
        ResourceRequest resource = new ResourceRequest("res-123", "DOCUMENT", "Report",
                Map.of("status", "draft"), Map.of("status", "published"));
        EventMetadataRequest metadata = new EventMetadataRequest("web-app", "tenant-001", "corr-123", "session-abc",
                Map.of("env", "test"), Map.of("custom", "value"));

        EventRequest request = new EventRequest(actor, action, resource, metadata);

        assertThat(request.actor().id()).isEqualTo("actor-123");
        assertThat(request.actor().attributes()).containsEntry("role", "admin");
        assertThat(request.action().type()).isEqualTo("UPDATE");
        assertThat(request.resource().before()).containsEntry("status", "draft");
        assertThat(request.metadata().correlationId()).isEqualTo("corr-123");
    }

    @Test
    void shouldHandleMultipleNestedValidationErrors() {
        ActorRequest invalidActor = new ActorRequest(null, null, null, null, null, null);
        ActionRequest invalidAction = new ActionRequest(null, null, null);
        ResourceRequest invalidResource = new ResourceRequest(null, null, null, null, null);
        EventMetadataRequest invalidMetadata = new EventMetadataRequest(null, null, null, null, null, null);

        EventRequest request = new EventRequest(invalidActor, invalidAction, invalidResource, invalidMetadata);

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(6);
    }
}
