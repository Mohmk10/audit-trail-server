package com.mohmk10.audittrail.sdk.model;

import java.util.Objects;

public class Action {
    private String type;
    private String description;
    private String category;

    private Action() {}

    private Action(ActionBuilder builder) {
        this.type = builder.type;
        this.description = builder.description;
        this.category = builder.category;
    }

    public static Action create(String description) {
        return builder().type("CREATE").description(description).build();
    }

    public static Action read(String description) {
        return builder().type("READ").description(description).build();
    }

    public static Action update(String description) {
        return builder().type("UPDATE").description(description).build();
    }

    public static Action delete(String description) {
        return builder().type("DELETE").description(description).build();
    }

    public static Action of(String type, String description) {
        return builder().type(type).description(description).build();
    }

    public static ActionBuilder builder() {
        return new ActionBuilder();
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return Objects.equals(type, action.type) && Objects.equals(description, action.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, description);
    }

    public static class ActionBuilder {
        private String type;
        private String description;
        private String category;

        public ActionBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ActionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ActionBuilder category(String category) {
            this.category = category;
            return this;
        }

        public Action build() {
            Objects.requireNonNull(type, "Action type is required");
            return new Action(this);
        }
    }
}
