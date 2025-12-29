package com.mohmk10.audittrail.ingestion.service;

import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ActionRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ActorRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventMetadataRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.ResourceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventValidationServiceImplTest {

    private EventValidationServiceImpl validationService;

    @BeforeEach
    void setUp() {
        validationService = new EventValidationServiceImpl();
    }

    private EventRequest createValidRequest() {
        ActorRequest actor = new ActorRequest("actor-123", "USER", "John", null, null, null);
        ActionRequest action = new ActionRequest("CREATE", "Created document", null);
        ResourceRequest resource = new ResourceRequest("res-123", "DOCUMENT", "Report", null, null);
        EventMetadataRequest metadata = new EventMetadataRequest("web-app", "tenant-001", null, null, null, null);
        return new EventRequest(actor, action, resource, metadata);
    }

    @Test
    void shouldReturnEmptyListForValidRequest() {
        EventRequest request = createValidRequest();

        List<String> violations = validationService.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidateAllActorTypes() {
        for (String type : new String[]{"USER", "SYSTEM", "SERVICE"}) {
            ActorRequest actor = new ActorRequest("actor-123", type, null, null, null, null);
            EventRequest request = new EventRequest(actor,
                    new ActionRequest("CREATE", null, null),
                    new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

            List<String> violations = validationService.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Test
    void shouldRejectInvalidActorType() {
        ActorRequest actor = new ActorRequest("actor-123", "INVALID_TYPE", null, null, null, null);
        EventRequest request = new EventRequest(actor,
                new ActionRequest("CREATE", null, null),
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0)).contains("Invalid actor type");
        assertThat(violations.get(0)).contains("INVALID_TYPE");
    }

    @Test
    void shouldValidateAllActionTypes() {
        for (String type : new String[]{"CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "LOGOUT"}) {
            ActionRequest action = new ActionRequest(type, null, null);
            EventRequest request = new EventRequest(
                    new ActorRequest("actor-123", "USER", null, null, null, null),
                    action,
                    new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

            List<String> violations = validationService.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Test
    void shouldRejectInvalidActionType() {
        ActionRequest action = new ActionRequest("INVALID_ACTION", null, null);
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                action,
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0)).contains("Invalid action type");
        assertThat(violations.get(0)).contains("INVALID_ACTION");
    }

    @Test
    void shouldValidateAllResourceTypes() {
        for (String type : new String[]{"DOCUMENT", "USER", "TRANSACTION", "CONFIG", "FILE", "API"}) {
            ResourceRequest resource = new ResourceRequest("res-123", type, null, null, null);
            EventRequest request = new EventRequest(
                    new ActorRequest("actor-123", "USER", null, null, null, null),
                    new ActionRequest("CREATE", null, null),
                    resource, null);

            List<String> violations = validationService.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Test
    void shouldRejectInvalidResourceType() {
        ResourceRequest resource = new ResourceRequest("res-123", "INVALID_RESOURCE", null, null, null);
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                new ActionRequest("CREATE", null, null),
                resource, null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0)).contains("Invalid resource type");
        assertThat(violations.get(0)).contains("INVALID_RESOURCE");
    }

    @Test
    void shouldReportMultipleViolations() {
        ActorRequest actor = new ActorRequest("actor-123", "INVALID_ACTOR", null, null, null, null);
        ActionRequest action = new ActionRequest("INVALID_ACTION", null, null);
        ResourceRequest resource = new ResourceRequest("res-123", "INVALID_RESOURCE", null, null, null);
        EventRequest request = new EventRequest(actor, action, resource, null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).hasSize(3);
    }

    @Test
    void shouldHandleCaseInsensitiveValidation() {
        ActorRequest actor = new ActorRequest("actor-123", "user", null, null, null, null);
        ActionRequest action = new ActionRequest("create", null, null);
        ResourceRequest resource = new ResourceRequest("res-123", "document", null, null, null);
        EventRequest request = new EventRequest(actor, action, resource, null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHandleMixedCaseTypes() {
        ActorRequest actor = new ActorRequest("actor-123", "UsEr", null, null, null, null);
        ActionRequest action = new ActionRequest("CrEaTe", null, null);
        ResourceRequest resource = new ResourceRequest("res-123", "DoCuMeNt", null, null, null);
        EventRequest request = new EventRequest(actor, action, resource, null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHandleNullActor() {
        EventRequest request = new EventRequest(null,
                new ActionRequest("CREATE", null, null),
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHandleNullAction() {
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                null,
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHandleNullResource() {
        EventRequest request = new EventRequest(
                new ActorRequest("actor-123", "USER", null, null, null, null),
                new ActionRequest("CREATE", null, null),
                null, null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHandleNullTypes() {
        ActorRequest actor = new ActorRequest("actor-123", null, null, null, null, null);
        ActionRequest action = new ActionRequest(null, null, null);
        ResourceRequest resource = new ResourceRequest("res-123", null, null, null, null);
        EventRequest request = new EventRequest(actor, action, resource, null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldIncludeValidTypesInErrorMessage() {
        ActorRequest actor = new ActorRequest("actor-123", "BAD_TYPE", null, null, null, null);
        EventRequest request = new EventRequest(actor,
                new ActionRequest("CREATE", null, null),
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        List<String> violations = validationService.validate(request);

        assertThat(violations.get(0)).contains("Valid types:");
        assertThat(violations.get(0)).contains("USER");
        assertThat(violations.get(0)).contains("SYSTEM");
    }

    @Test
    void shouldValidateEmptyStringTypes() {
        ActorRequest actor = new ActorRequest("actor-123", "", null, null, null, null);
        ActionRequest action = new ActionRequest("", null, null);
        ResourceRequest resource = new ResourceRequest("res-123", "", null, null, null);
        EventRequest request = new EventRequest(actor, action, resource, null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).hasSize(3);
    }

    @Test
    void shouldValidateWhitespaceTypes() {
        ActorRequest actor = new ActorRequest("actor-123", "  ", null, null, null, null);
        EventRequest request = new EventRequest(actor,
                new ActionRequest("CREATE", null, null),
                new ResourceRequest("res-123", "DOCUMENT", null, null, null), null);

        List<String> violations = validationService.validate(request);

        assertThat(violations).hasSize(1);
    }
}
