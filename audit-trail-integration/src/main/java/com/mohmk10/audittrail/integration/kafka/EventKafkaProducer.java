package com.mohmk10.audittrail.integration.kafka;

import com.mohmk10.audittrail.core.domain.Event;

import java.util.concurrent.CompletableFuture;

public interface EventKafkaProducer {

    CompletableFuture<Void> send(Event event);

    CompletableFuture<Void> send(String topic, Event event);

    CompletableFuture<Void> send(String topic, String key, Event event);
}
