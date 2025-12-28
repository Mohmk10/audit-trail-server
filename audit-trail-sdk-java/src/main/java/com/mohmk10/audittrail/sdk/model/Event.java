package com.mohmk10.audittrail.sdk.model;

import java.util.Objects;

public class Event {
    private Actor actor;
    private Action action;
    private Resource resource;
    private EventMetadata metadata;

    private Event() {}

    private Event(EventBuilder builder) {
        this.actor = builder.actor;
        this.action = builder.action;
        this.resource = builder.resource;
        this.metadata = builder.metadata;
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public Actor getActor() {
        return actor;
    }

    public Action getAction() {
        return action;
    }

    public Resource getResource() {
        return resource;
    }

    public EventMetadata getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(actor, event.actor) &&
               Objects.equals(action, event.action) &&
               Objects.equals(resource, event.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actor, action, resource);
    }

    public static class EventBuilder {
        private Actor actor;
        private Action action;
        private Resource resource;
        private EventMetadata metadata;

        public EventBuilder actor(Actor actor) {
            this.actor = actor;
            return this;
        }

        public EventBuilder action(Action action) {
            this.action = action;
            return this;
        }

        public EventBuilder resource(Resource resource) {
            this.resource = resource;
            return this;
        }

        public EventBuilder metadata(EventMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Event build() {
            Objects.requireNonNull(actor, "Event actor is required");
            Objects.requireNonNull(action, "Event action is required");
            Objects.requireNonNull(resource, "Event resource is required");
            return new Event(this);
        }
    }
}
