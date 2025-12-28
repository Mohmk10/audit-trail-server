package com.mohmk10.audittrail.core.event;

import com.mohmk10.audittrail.core.domain.Event;
import org.springframework.context.ApplicationEvent;

public class EventStoredEvent extends ApplicationEvent {

    private final Event event;

    public EventStoredEvent(Object source, Event event) {
        super(source);
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}
