package com.mohmk10.audittrail.sdk.model;

import java.time.Instant;
import java.util.UUID;

public class EventResponse {
    private UUID id;
    private Instant timestamp;
    private String hash;
    private String status;

    public EventResponse() {}

    public EventResponse(UUID id, Instant timestamp, String hash, String status) {
        this.id = id;
        this.timestamp = timestamp;
        this.hash = hash;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "EventResponse{id=" + id + ", timestamp=" + timestamp + ", hash='" + hash + "', status='" + status + "'}";
    }
}
