package com.mohmk10.audittrail.integration.kafka;

import com.mohmk10.audittrail.core.domain.Event;

public interface EventKafkaConsumer {

    void consume(Event event);

    void consume(String key, Event event);
}
