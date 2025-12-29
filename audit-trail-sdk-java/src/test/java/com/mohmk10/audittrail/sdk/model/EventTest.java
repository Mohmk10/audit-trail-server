package com.mohmk10.audittrail.sdk.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTest {

    @Test
    void shouldCreateEventWithBuilder() {
        Actor actor = Actor.user("user-123", "John Doe");
        Action action = Action.create("Created document");
        Resource resource = Resource.document("doc-456", "report.pdf");

        Event event = Event.builder()
                .actor(actor)
                .action(action)
                .resource(resource)
                .build();

        assertThat(event.getActor()).isEqualTo(actor);
        assertThat(event.getAction()).isEqualTo(action);
        assertThat(event.getResource()).isEqualTo(resource);
    }

    @Test
    void shouldThrowWhenActorIsNull() {
        Action action = Action.create("Created document");
        Resource resource = Resource.document("doc-456", "report.pdf");

        assertThatThrownBy(() -> Event.builder()
                .action(action)
                .resource(resource)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Event actor is required");
    }

    @Test
    void shouldThrowWhenActionIsNull() {
        Actor actor = Actor.user("user-123", "John Doe");
        Resource resource = Resource.document("doc-456", "report.pdf");

        assertThatThrownBy(() -> Event.builder()
                .actor(actor)
                .resource(resource)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Event action is required");
    }

    @Test
    void shouldThrowWhenResourceIsNull() {
        Actor actor = Actor.user("user-123", "John Doe");
        Action action = Action.create("Created document");

        assertThatThrownBy(() -> Event.builder()
                .actor(actor)
                .action(action)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Event resource is required");
    }

    @Test
    void shouldCreateEventWithMetadata() {
        Actor actor = Actor.user("user-123", "John");
        Action action = Action.read("Read document");
        Resource resource = Resource.document("doc-1", "file.pdf");
        EventMetadata metadata = EventMetadata.builder()
                .source("web-app")
                .tenantId("tenant-001")
                .correlationId("corr-123")
                .build();

        Event event = Event.builder()
                .actor(actor)
                .action(action)
                .resource(resource)
                .metadata(metadata)
                .build();

        assertThat(event.getMetadata()).isNotNull();
        assertThat(event.getMetadata().getSource()).isEqualTo("web-app");
        assertThat(event.getMetadata().getTenantId()).isEqualTo("tenant-001");
    }

    @Test
    void shouldHaveNullMetadataWhenNotSet() {
        Actor actor = Actor.user("user-123", "John");
        Action action = Action.read("Read document");
        Resource resource = Resource.document("doc-1", "file.pdf");

        Event event = Event.builder()
                .actor(actor)
                .action(action)
                .resource(resource)
                .build();

        assertThat(event.getMetadata()).isNull();
    }

    @Test
    void shouldSupportEqualsBasedOnActorActionResource() {
        Actor actor = Actor.user("user-1", "User");
        Action action = Action.create("Create");
        Resource resource = Resource.document("doc-1", "file.pdf");

        Event event1 = Event.builder()
                .actor(actor)
                .action(action)
                .resource(resource)
                .build();

        Event event2 = Event.builder()
                .actor(actor)
                .action(action)
                .resource(resource)
                .build();

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentActors() {
        Action action = Action.create("Create");
        Resource resource = Resource.document("doc-1", "file.pdf");

        Event event1 = Event.builder()
                .actor(Actor.user("user-1", "User1"))
                .action(action)
                .resource(resource)
                .build();

        Event event2 = Event.builder()
                .actor(Actor.user("user-2", "User2"))
                .action(action)
                .resource(resource)
                .build();

        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    void shouldNotBeEqualWithDifferentActions() {
        Actor actor = Actor.user("user-1", "User");
        Resource resource = Resource.document("doc-1", "file.pdf");

        Event event1 = Event.builder()
                .actor(actor)
                .action(Action.create("Create"))
                .resource(resource)
                .build();

        Event event2 = Event.builder()
                .actor(actor)
                .action(Action.delete("Delete"))
                .resource(resource)
                .build();

        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    void shouldNotBeEqualWithDifferentResources() {
        Actor actor = Actor.user("user-1", "User");
        Action action = Action.create("Create");

        Event event1 = Event.builder()
                .actor(actor)
                .action(action)
                .resource(Resource.document("doc-1", "file1.pdf"))
                .build();

        Event event2 = Event.builder()
                .actor(actor)
                .action(action)
                .resource(Resource.document("doc-2", "file2.pdf"))
                .build();

        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    void shouldCreateCompleteAuditEvent() {
        Actor actor = Actor.builder()
                .id("user-123")
                .type("USER")
                .name("John Doe")
                .ip("192.168.1.1")
                .attribute("department", "IT")
                .build();

        Action action = Action.builder()
                .type("document.publish")
                .category("documents")
                .description("Published document for review")
                .build();

        Resource resource = Resource.builder()
                .id("doc-456")
                .type("DOCUMENT")
                .name("quarterly-report.pdf")
                .before("status", "draft")
                .after("status", "published")
                .build();

        EventMetadata metadata = EventMetadata.builder()
                .source("web-app")
                .tenantId("tenant-001")
                .correlationId("corr-uuid")
                .sessionId("sess-uuid")
                .tag("env", "prod")
                .build();

        Event event = Event.builder()
                .actor(actor)
                .action(action)
                .resource(resource)
                .metadata(metadata)
                .build();

        assertThat(event.getActor().getName()).isEqualTo("John Doe");
        assertThat(event.getAction().getType()).isEqualTo("document.publish");
        assertThat(event.getResource().getName()).isEqualTo("quarterly-report.pdf");
        assertThat(event.getMetadata().getSource()).isEqualTo("web-app");
    }

    @Test
    void shouldCreateEventWithUserLogin() {
        Actor actor = Actor.builder()
                .id("user-001")
                .type("USER")
                .name("admin@company.com")
                .ip("10.0.0.50")
                .userAgent("Mozilla/5.0")
                .build();

        Action action = Action.builder()
                .type("user.login")
                .category("authentication")
                .description("User logged in via SSO")
                .build();

        Resource resource = Resource.builder()
                .id("session-abc123")
                .type("SESSION")
                .name("Web Session")
                .build();

        Event event = Event.builder()
                .actor(actor)
                .action(action)
                .resource(resource)
                .build();

        assertThat(event.getActor().getType()).isEqualTo("USER");
        assertThat(event.getAction().getCategory()).isEqualTo("authentication");
        assertThat(event.getResource().getType()).isEqualTo("SESSION");
    }

    @Test
    void shouldCreateEventWithDataModification() {
        Resource resource = Resource.builder()
                .id("user-profile-001")
                .type("USER_PROFILE")
                .name("User Profile")
                .before("email", "old@email.com")
                .before("phone", "123-456-7890")
                .after("email", "new@email.com")
                .after("phone", "098-765-4321")
                .build();

        Event event = Event.builder()
                .actor(Actor.user("admin-001", "Admin"))
                .action(Action.update("Updated user profile"))
                .resource(resource)
                .build();

        assertThat(event.getResource().getBefore()).containsEntry("email", "old@email.com");
        assertThat(event.getResource().getAfter()).containsEntry("email", "new@email.com");
    }

    @Test
    void shouldCreateEventWithSystemActor() {
        Event event = Event.builder()
                .actor(Actor.system("scheduler"))
                .action(Action.of("CLEANUP", "Scheduled cleanup task"))
                .resource(Resource.of("logs-001", "LOGS", "Application Logs"))
                .build();

        assertThat(event.getActor().getType()).isEqualTo("SYSTEM");
        assertThat(event.getAction().getType()).isEqualTo("CLEANUP");
    }

    @Test
    void shouldCreateEventWithServiceActor() {
        Event event = Event.builder()
                .actor(Actor.service("payment-service", "Payment Service"))
                .action(Action.create("Created transaction"))
                .resource(Resource.of("txn-001", "TRANSACTION", "Payment Transaction"))
                .build();

        assertThat(event.getActor().getType()).isEqualTo("SERVICE");
        assertThat(event.getActor().getName()).isEqualTo("Payment Service");
    }
}
