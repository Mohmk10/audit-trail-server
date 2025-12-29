package com.mohmk10.audittrail.integration.kafka;

import com.mohmk10.audittrail.core.domain.Event;

public record KafkaEventReceivedEvent(Event event) {}
