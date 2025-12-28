package com.mohmk10.audittrail.sdk;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mohmk10.audittrail.sdk.model.Event;
import com.mohmk10.audittrail.sdk.model.EventResponse;

public interface AuditTrailClient extends Closeable {

    EventResponse log(Event event);

    List<EventResponse> logBatch(List<Event> events);

    Optional<EventResponse> findById(UUID id);

    CompletableFuture<EventResponse> logAsync(Event event);

    CompletableFuture<List<EventResponse>> logBatchAsync(List<Event> events);

    static AuditTrailClientBuilder builder() {
        return new AuditTrailClientBuilder();
    }
}
