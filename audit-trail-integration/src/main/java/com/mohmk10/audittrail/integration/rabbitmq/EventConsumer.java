package com.mohmk10.audittrail.integration.rabbitmq;

import com.mohmk10.audittrail.core.domain.Event;

public interface EventConsumer {

    void consumeEvent(Event event);
}
