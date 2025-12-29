package com.mohmk10.audittrail.core.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ActorTest {

    @Test
    void shouldCreateActorWithAllFields() {
        Actor actor = new Actor(
                "user-123",
                Actor.ActorType.USER,
                "John Doe",
                "192.168.1.1",
                "Mozilla/5.0",
                Map.of("role", "admin", "department", "IT")
        );

        assertThat(actor.id()).isEqualTo("user-123");
        assertThat(actor.type()).isEqualTo(Actor.ActorType.USER);
        assertThat(actor.name()).isEqualTo("John Doe");
        assertThat(actor.ip()).isEqualTo("192.168.1.1");
        assertThat(actor.userAgent()).isEqualTo("Mozilla/5.0");
        assertThat(actor.attributes()).containsEntry("role", "admin");
        assertThat(actor.attributes()).containsEntry("department", "IT");
    }

    @Test
    void shouldCreateActorWithMinimalFields() {
        Actor actor = new Actor(
                "user-min",
                Actor.ActorType.SYSTEM,
                "System",
                null,
                null,
                null
        );

        assertThat(actor.id()).isEqualTo("user-min");
        assertThat(actor.type()).isEqualTo(Actor.ActorType.SYSTEM);
        assertThat(actor.name()).isEqualTo("System");
        assertThat(actor.ip()).isNull();
        assertThat(actor.userAgent()).isNull();
        assertThat(actor.attributes()).isNull();
    }

    @Test
    void shouldSupportAllActorTypes() {
        Actor userActor = new Actor("u1", Actor.ActorType.USER, "User", null, null, null);
        Actor systemActor = new Actor("s1", Actor.ActorType.SYSTEM, "System", null, null, null);
        Actor serviceActor = new Actor("srv1", Actor.ActorType.SERVICE, "Service", null, null, null);

        assertThat(userActor.type()).isEqualTo(Actor.ActorType.USER);
        assertThat(systemActor.type()).isEqualTo(Actor.ActorType.SYSTEM);
        assertThat(serviceActor.type()).isEqualTo(Actor.ActorType.SERVICE);
    }

    @Test
    void shouldHandleNullAttributes() {
        Actor actor = new Actor("id", Actor.ActorType.USER, "Name", "ip", "ua", null);

        assertThat(actor.attributes()).isNull();
    }

    @Test
    void shouldHandleEmptyAttributes() {
        Actor actor = new Actor("id", Actor.ActorType.USER, "Name", "ip", "ua", Map.of());

        assertThat(actor.attributes()).isEmpty();
    }

    @Test
    void shouldPreserveIpAddress() {
        Actor actor = new Actor("id", Actor.ActorType.USER, "Name", "10.0.0.100", null, null);

        assertThat(actor.ip()).isEqualTo("10.0.0.100");
    }

    @Test
    void shouldPreserveUserAgent() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        Actor actor = new Actor("id", Actor.ActorType.USER, "Name", null, userAgent, null);

        assertThat(actor.userAgent()).isEqualTo(userAgent);
    }

    @Test
    void shouldSupportRecordEquality() {
        Actor actor1 = new Actor("id", Actor.ActorType.USER, "Name", "ip", "ua", Map.of("k", "v"));
        Actor actor2 = new Actor("id", Actor.ActorType.USER, "Name", "ip", "ua", Map.of("k", "v"));

        assertThat(actor1).isEqualTo(actor2);
        assertThat(actor1.hashCode()).isEqualTo(actor2.hashCode());
    }

    @Test
    void shouldDifferentiateNonEqualActors() {
        Actor actor1 = new Actor("id1", Actor.ActorType.USER, "Name", null, null, null);
        Actor actor2 = new Actor("id2", Actor.ActorType.USER, "Name", null, null, null);

        assertThat(actor1).isNotEqualTo(actor2);
    }
}
