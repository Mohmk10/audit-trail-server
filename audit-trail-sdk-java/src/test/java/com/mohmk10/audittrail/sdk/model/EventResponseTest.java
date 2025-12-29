package com.mohmk10.audittrail.sdk.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventResponseTest {

    @Test
    void shouldCreateWithDefaultConstructor() {
        EventResponse response = new EventResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getTimestamp()).isNull();
        assertThat(response.getHash()).isNull();
        assertThat(response.getStatus()).isNull();
    }

    @Test
    void shouldCreateWithAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();
        String hash = "abc123hash";
        String status = "STORED";

        EventResponse response = new EventResponse(id, timestamp, hash, status);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getHash()).isEqualTo(hash);
        assertThat(response.getStatus()).isEqualTo(status);
    }

    @Test
    void shouldSetAndGetId() {
        EventResponse response = new EventResponse();
        UUID id = UUID.randomUUID();

        response.setId(id);

        assertThat(response.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetTimestamp() {
        EventResponse response = new EventResponse();
        Instant timestamp = Instant.parse("2024-01-15T10:30:00Z");

        response.setTimestamp(timestamp);

        assertThat(response.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldSetAndGetHash() {
        EventResponse response = new EventResponse();
        String hash = "sha256:abc123def456";

        response.setHash(hash);

        assertThat(response.getHash()).isEqualTo(hash);
    }

    @Test
    void shouldSetAndGetStatus() {
        EventResponse response = new EventResponse();
        String status = "VERIFIED";

        response.setStatus(status);

        assertThat(response.getStatus()).isEqualTo(status);
    }

    @Test
    void shouldHaveToStringRepresentation() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();

        EventResponse response = new EventResponse(id, timestamp, "hash123", "STORED");

        String toString = response.toString();

        assertThat(toString).contains(id.toString());
        assertThat(toString).contains("hash123");
        assertThat(toString).contains("STORED");
    }

    @Test
    void shouldHandleNullValues() {
        EventResponse response = new EventResponse(null, null, null, null);

        assertThat(response.getId()).isNull();
        assertThat(response.getTimestamp()).isNull();
        assertThat(response.getHash()).isNull();
        assertThat(response.getStatus()).isNull();
    }

    @Test
    void shouldSupportVariousStatusValues() {
        EventResponse response = new EventResponse();

        response.setStatus("PENDING");
        assertThat(response.getStatus()).isEqualTo("PENDING");

        response.setStatus("STORED");
        assertThat(response.getStatus()).isEqualTo("STORED");

        response.setStatus("VERIFIED");
        assertThat(response.getStatus()).isEqualTo("VERIFIED");

        response.setStatus("FAILED");
        assertThat(response.getStatus()).isEqualTo("FAILED");
    }

    @Test
    void shouldAllowUpdatingValues() {
        UUID originalId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();

        EventResponse response = new EventResponse(originalId, Instant.now(), "hash1", "PENDING");

        response.setId(newId);
        response.setHash("hash2");
        response.setStatus("STORED");

        assertThat(response.getId()).isEqualTo(newId);
        assertThat(response.getHash()).isEqualTo("hash2");
        assertThat(response.getStatus()).isEqualTo("STORED");
    }
}
