package com.mohmk10.audittrail.sdk.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActionTest {

    @Test
    void shouldCreateActionWithBuilder() {
        Action action = Action.builder()
                .type("user.login")
                .category("authentication")
                .description("User logged in successfully")
                .build();

        assertThat(action.getType()).isEqualTo("user.login");
        assertThat(action.getCategory()).isEqualTo("authentication");
        assertThat(action.getDescription()).isEqualTo("User logged in successfully");
    }

    @Test
    void shouldThrowWhenTypeIsNull() {
        assertThatThrownBy(() -> Action.builder()
                .description("Some action")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Action type is required");
    }

    @Test
    void shouldCreateCreateActionWithFactoryMethod() {
        Action action = Action.create("Created a new document");

        assertThat(action.getType()).isEqualTo("CREATE");
        assertThat(action.getDescription()).isEqualTo("Created a new document");
    }

    @Test
    void shouldCreateReadActionWithFactoryMethod() {
        Action action = Action.read("Read user profile");

        assertThat(action.getType()).isEqualTo("READ");
        assertThat(action.getDescription()).isEqualTo("Read user profile");
    }

    @Test
    void shouldCreateUpdateActionWithFactoryMethod() {
        Action action = Action.update("Updated settings");

        assertThat(action.getType()).isEqualTo("UPDATE");
        assertThat(action.getDescription()).isEqualTo("Updated settings");
    }

    @Test
    void shouldCreateDeleteActionWithFactoryMethod() {
        Action action = Action.delete("Deleted record");

        assertThat(action.getType()).isEqualTo("DELETE");
        assertThat(action.getDescription()).isEqualTo("Deleted record");
    }

    @Test
    void shouldCreateCustomActionWithOfMethod() {
        Action action = Action.of("CUSTOM_ACTION", "Custom action description");

        assertThat(action.getType()).isEqualTo("CUSTOM_ACTION");
        assertThat(action.getDescription()).isEqualTo("Custom action description");
    }

    @Test
    void shouldCreateActionWithCategory() {
        Action action = Action.builder()
                .type("PAYMENT")
                .category("financial")
                .description("Processed payment")
                .build();

        assertThat(action.getCategory()).isEqualTo("financial");
    }

    @Test
    void shouldHaveNullFieldsWhenNotSet() {
        Action action = Action.builder()
                .type("TEST")
                .build();

        assertThat(action.getCategory()).isNull();
        assertThat(action.getDescription()).isNull();
    }

    @Test
    void shouldSupportEqualsBasedOnTypeAndDescription() {
        Action action1 = Action.builder()
                .type("user.login")
                .description("desc")
                .category("auth")
                .build();

        Action action2 = Action.builder()
                .type("user.login")
                .description("desc")
                .category("different")
                .build();

        assertThat(action1).isEqualTo(action2);
        assertThat(action1.hashCode()).isEqualTo(action2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentTypes() {
        Action action1 = Action.builder()
                .type("user.login")
                .build();

        Action action2 = Action.builder()
                .type("user.logout")
                .build();

        assertThat(action1).isNotEqualTo(action2);
    }

    @Test
    void shouldNotBeEqualWithDifferentDescriptions() {
        Action action1 = Action.builder()
                .type("user.login")
                .description("desc1")
                .build();

        Action action2 = Action.builder()
                .type("user.login")
                .description("desc2")
                .build();

        assertThat(action1).isNotEqualTo(action2);
    }

    @Test
    void shouldCreateActionWithAllFields() {
        Action action = Action.builder()
                .type("document.export")
                .category("documents")
                .description("Export document to PDF")
                .build();

        assertThat(action.getType()).isEqualTo("document.export");
        assertThat(action.getCategory()).isEqualTo("documents");
        assertThat(action.getDescription()).isEqualTo("Export document to PDF");
    }

    @Test
    void shouldCreateActionWithLongDescription() {
        String longDescription = "This is a very long description that explains in detail " +
                "what happened during this action including all relevant context and information " +
                "that might be useful for auditing purposes.";

        Action action = Action.builder()
                .type("audit.event")
                .description(longDescription)
                .build();

        assertThat(action.getDescription()).isEqualTo(longDescription);
    }

    @Test
    void shouldSupportCRUDOperations() {
        assertThat(Action.create("create").getType()).isEqualTo("CREATE");
        assertThat(Action.read("read").getType()).isEqualTo("READ");
        assertThat(Action.update("update").getType()).isEqualTo("UPDATE");
        assertThat(Action.delete("delete").getType()).isEqualTo("DELETE");
    }
}
