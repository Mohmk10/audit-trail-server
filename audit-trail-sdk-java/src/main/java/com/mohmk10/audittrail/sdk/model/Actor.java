package com.mohmk10.audittrail.sdk.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Actor {
    private String id;
    private String type;
    private String name;
    private String ip;
    private String userAgent;
    private Map<String, String> attributes;

    private Actor() {}

    private Actor(ActorBuilder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.ip = builder.ip;
        this.userAgent = builder.userAgent;
        this.attributes = builder.attributes;
    }

    public static Actor user(String id, String name) {
        return builder().id(id).type("USER").name(name).build();
    }

    public static Actor system(String id) {
        return builder().id(id).type("SYSTEM").build();
    }

    public static Actor service(String id, String name) {
        return builder().id(id).type("SERVICE").name(name).build();
    }

    public static ActorBuilder builder() {
        return new ActorBuilder();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public ActorBuilder toBuilder() {
        return new ActorBuilder()
                .id(this.id)
                .type(this.type)
                .name(this.name)
                .ip(this.ip)
                .userAgent(this.userAgent)
                .attributes(this.attributes != null ? this.attributes : new HashMap<>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Actor actor = (Actor) o;
        return Objects.equals(id, actor.id) && Objects.equals(type, actor.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    public static class ActorBuilder {
        private String id;
        private String type;
        private String name;
        private String ip;
        private String userAgent;
        private Map<String, String> attributes = new HashMap<>();

        public ActorBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ActorBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ActorBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ActorBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public ActorBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public ActorBuilder attribute(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }

        public ActorBuilder attributes(Map<String, String> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        public Actor build() {
            Objects.requireNonNull(id, "Actor id is required");
            Objects.requireNonNull(type, "Actor type is required");
            return new Actor(this);
        }
    }
}
