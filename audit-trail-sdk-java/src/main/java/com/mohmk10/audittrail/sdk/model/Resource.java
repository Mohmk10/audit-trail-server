package com.mohmk10.audittrail.sdk.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Resource {
    private String id;
    private String type;
    private String name;
    private Map<String, Object> before;
    private Map<String, Object> after;

    private Resource() {}

    private Resource(ResourceBuilder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.before = builder.before.isEmpty() ? null : builder.before;
        this.after = builder.after.isEmpty() ? null : builder.after;
    }

    public static Resource of(String id, String type, String name) {
        return builder().id(id).type(type).name(name).build();
    }

    public static Resource document(String id, String name) {
        return builder().id(id).type("DOCUMENT").name(name).build();
    }

    public static Resource user(String id, String name) {
        return builder().id(id).type("USER").name(name).build();
    }

    public static Resource file(String id, String name) {
        return builder().id(id).type("FILE").name(name).build();
    }

    public static Resource config(String id, String name) {
        return builder().id(id).type("CONFIG").name(name).build();
    }

    public static ResourceBuilder builder() {
        return new ResourceBuilder();
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

    public Map<String, Object> getBefore() {
        return before;
    }

    public Map<String, Object> getAfter() {
        return after;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(id, resource.id) && Objects.equals(type, resource.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    public static class ResourceBuilder {
        private String id;
        private String type;
        private String name;
        private Map<String, Object> before = new HashMap<>();
        private Map<String, Object> after = new HashMap<>();

        public ResourceBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ResourceBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ResourceBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ResourceBuilder before(String key, Object value) {
            this.before.put(key, value);
            return this;
        }

        public ResourceBuilder before(Map<String, Object> before) {
            this.before.putAll(before);
            return this;
        }

        public ResourceBuilder after(String key, Object value) {
            this.after.put(key, value);
            return this;
        }

        public ResourceBuilder after(Map<String, Object> after) {
            this.after.putAll(after);
            return this;
        }

        public Resource build() {
            Objects.requireNonNull(id, "Resource id is required");
            Objects.requireNonNull(type, "Resource type is required");
            return new Resource(this);
        }
    }
}
