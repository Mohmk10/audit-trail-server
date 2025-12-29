package com.mohmk10.audittrail.sdk.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActorTest {

    @Test
    void shouldCreateActorWithBuilder() {
        Actor actor = Actor.builder()
                .id("user-123")
                .type("USER")
                .name("John Doe")
                .build();

        assertThat(actor.getId()).isEqualTo("user-123");
        assertThat(actor.getType()).isEqualTo("USER");
        assertThat(actor.getName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> Actor.builder()
                .type("USER")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Actor id is required");
    }

    @Test
    void shouldThrowWhenTypeIsNull() {
        assertThatThrownBy(() -> Actor.builder()
                .id("user-123")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Actor type is required");
    }

    @Test
    void shouldCreateActorWithAttributes() {
        Actor actor = Actor.builder()
                .id("user-123")
                .type("USER")
                .attribute("department", "Engineering")
                .attribute("role", "Developer")
                .build();

        assertThat(actor.getAttributes()).containsEntry("department", "Engineering");
        assertThat(actor.getAttributes()).containsEntry("role", "Developer");
    }

    @Test
    void shouldCreateActorWithAttributesMap() {
        Map<String, String> attributes = Map.of("key1", "value1", "key2", "value2");

        Actor actor = Actor.builder()
                .id("user-123")
                .type("USER")
                .attributes(attributes)
                .build();

        assertThat(actor.getAttributes()).containsAllEntriesOf(attributes);
    }

    @Test
    void shouldCreateUserActorWithFactoryMethod() {
        Actor actor = Actor.user("user-456", "Jane Doe");

        assertThat(actor.getId()).isEqualTo("user-456");
        assertThat(actor.getType()).isEqualTo("USER");
        assertThat(actor.getName()).isEqualTo("Jane Doe");
    }

    @Test
    void shouldCreateSystemActorWithFactoryMethod() {
        Actor actor = Actor.system("scheduler-service");

        assertThat(actor.getId()).isEqualTo("scheduler-service");
        assertThat(actor.getType()).isEqualTo("SYSTEM");
        assertThat(actor.getName()).isNull();
    }

    @Test
    void shouldCreateServiceActorWithFactoryMethod() {
        Actor actor = Actor.service("payment-service", "Payment Service");

        assertThat(actor.getId()).isEqualTo("payment-service");
        assertThat(actor.getType()).isEqualTo("SERVICE");
        assertThat(actor.getName()).isEqualTo("Payment Service");
    }

    @Test
    void shouldCreateActorWithIp() {
        Actor actor = Actor.builder()
                .id("user-123")
                .type("USER")
                .ip("192.168.1.100")
                .build();

        assertThat(actor.getIp()).isEqualTo("192.168.1.100");
    }

    @Test
    void shouldCreateActorWithUserAgent() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        Actor actor = Actor.builder()
                .id("user-123")
                .type("USER")
                .userAgent(userAgent)
                .build();

        assertThat(actor.getUserAgent()).isEqualTo(userAgent);
    }

    @Test
    void shouldHaveNullFieldsWhenNotSet() {
        Actor actor = Actor.builder()
                .id("user-123")
                .type("USER")
                .build();

        assertThat(actor.getName()).isNull();
        assertThat(actor.getIp()).isNull();
        assertThat(actor.getUserAgent()).isNull();
    }

    @Test
    void shouldSupportEqualsBasedOnIdAndType() {
        Actor actor1 = Actor.builder()
                .id("user-123")
                .type("USER")
                .name("John")
                .build();

        Actor actor2 = Actor.builder()
                .id("user-123")
                .type("USER")
                .name("Different Name")
                .build();

        assertThat(actor1).isEqualTo(actor2);
        assertThat(actor1.hashCode()).isEqualTo(actor2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentIds() {
        Actor actor1 = Actor.builder()
                .id("user-123")
                .type("USER")
                .build();

        Actor actor2 = Actor.builder()
                .id("user-456")
                .type("USER")
                .build();

        assertThat(actor1).isNotEqualTo(actor2);
    }

    @Test
    void shouldNotBeEqualWithDifferentTypes() {
        Actor actor1 = Actor.builder()
                .id("user-123")
                .type("USER")
                .build();

        Actor actor2 = Actor.builder()
                .id("user-123")
                .type("SYSTEM")
                .build();

        assertThat(actor1).isNotEqualTo(actor2);
    }

    @Test
    void shouldCreateActorWithAllFields() {
        Actor actor = Actor.builder()
                .id("user-123")
                .type("ADMIN")
                .name("Admin User")
                .ip("10.0.0.1")
                .userAgent("CustomAgent/1.0")
                .attribute("role", "admin")
                .build();

        assertThat(actor.getId()).isEqualTo("user-123");
        assertThat(actor.getType()).isEqualTo("ADMIN");
        assertThat(actor.getName()).isEqualTo("Admin User");
        assertThat(actor.getIp()).isEqualTo("10.0.0.1");
        assertThat(actor.getUserAgent()).isEqualTo("CustomAgent/1.0");
        assertThat(actor.getAttributes()).containsEntry("role", "admin");
    }

    @Test
    void shouldSupportToBuilder() {
        Actor original = Actor.builder()
                .id("user-123")
                .type("USER")
                .name("Original")
                .build();

        Actor modified = original.toBuilder()
                .name("Modified")
                .ip("192.168.1.1")
                .build();

        assertThat(modified.getId()).isEqualTo("user-123");
        assertThat(modified.getType()).isEqualTo("USER");
        assertThat(modified.getName()).isEqualTo("Modified");
        assertThat(modified.getIp()).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldMergeAttributes() {
        Map<String, String> attrs1 = Map.of("key1", "value1");

        Actor actor = Actor.builder()
                .id("user-123")
                .type("USER")
                .attributes(attrs1)
                .attribute("key2", "value2")
                .build();

        assertThat(actor.getAttributes()).hasSize(2);
        assertThat(actor.getAttributes()).containsEntry("key1", "value1");
        assertThat(actor.getAttributes()).containsEntry("key2", "value2");
    }
}
