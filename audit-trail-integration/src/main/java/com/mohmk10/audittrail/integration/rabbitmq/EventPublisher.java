package com.mohmk10.audittrail.integration.rabbitmq;

import com.mohmk10.audittrail.core.domain.Event;

import java.util.List;

public interface EventPublisher {

    void publishEvent(Event event);

    void publishEventBatch(List<Event> events);
}
