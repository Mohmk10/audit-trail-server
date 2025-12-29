package com.mohmk10.audittrail.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionTest {

    @Test
    void shouldCreateActionWithType() {
        Action action = new Action(Action.ActionType.CREATE, null, null);

        assertThat(action.type()).isEqualTo(Action.ActionType.CREATE);
        assertThat(action.description()).isNull();
        assertThat(action.category()).isNull();
    }

    @Test
    void shouldCreateActionWithDescription() {
        Action action = new Action(Action.ActionType.UPDATE, "Updated user profile", null);

        assertThat(action.type()).isEqualTo(Action.ActionType.UPDATE);
        assertThat(action.description()).isEqualTo("Updated user profile");
    }

    @Test
    void shouldCreateActionWithCategory() {
        Action action = new Action(Action.ActionType.DELETE, "Deleted document", "DOCUMENT_MANAGEMENT");

        assertThat(action.type()).isEqualTo(Action.ActionType.DELETE);
        assertThat(action.category()).isEqualTo("DOCUMENT_MANAGEMENT");
    }

    @Test
    void shouldCreateActionWithAllFields() {
        Action action = new Action(
                Action.ActionType.EXPORT,
                "Exported report to PDF",
                "REPORTING"
        );

        assertThat(action.type()).isEqualTo(Action.ActionType.EXPORT);
        assertThat(action.description()).isEqualTo("Exported report to PDF");
        assertThat(action.category()).isEqualTo("REPORTING");
    }

    @Test
    void shouldSupportAllActionTypes() {
        assertThat(Action.ActionType.CREATE).isNotNull();
        assertThat(Action.ActionType.READ).isNotNull();
        assertThat(Action.ActionType.UPDATE).isNotNull();
        assertThat(Action.ActionType.DELETE).isNotNull();
        assertThat(Action.ActionType.APPROVE).isNotNull();
        assertThat(Action.ActionType.REJECT).isNotNull();
        assertThat(Action.ActionType.LOGIN).isNotNull();
        assertThat(Action.ActionType.LOGOUT).isNotNull();
        assertThat(Action.ActionType.EXPORT).isNotNull();
        assertThat(Action.ActionType.IMPORT).isNotNull();
        assertThat(Action.ActionType.ARCHIVE).isNotNull();
        assertThat(Action.ActionType.RESTORE).isNotNull();
    }

    @Test
    void shouldHaveCorrectActionTypeValues() {
        assertThat(Action.ActionType.values()).hasSize(12);
        assertThat(Action.ActionType.valueOf("CREATE")).isEqualTo(Action.ActionType.CREATE);
        assertThat(Action.ActionType.valueOf("LOGIN")).isEqualTo(Action.ActionType.LOGIN);
    }

    @Test
    void shouldSupportRecordEquality() {
        Action action1 = new Action(Action.ActionType.CREATE, "desc", "cat");
        Action action2 = new Action(Action.ActionType.CREATE, "desc", "cat");

        assertThat(action1).isEqualTo(action2);
        assertThat(action1.hashCode()).isEqualTo(action2.hashCode());
    }

    @Test
    void shouldDifferentiateNonEqualActions() {
        Action action1 = new Action(Action.ActionType.CREATE, "desc", "cat");
        Action action2 = new Action(Action.ActionType.UPDATE, "desc", "cat");

        assertThat(action1).isNotEqualTo(action2);
    }

    @Test
    void shouldHandleNullDescription() {
        Action action = new Action(Action.ActionType.READ, null, "AUDIT");

        assertThat(action.description()).isNull();
        assertThat(action.category()).isEqualTo("AUDIT");
    }

    @Test
    void shouldHandleNullCategory() {
        Action action = new Action(Action.ActionType.DELETE, "Deleted item", null);

        assertThat(action.description()).isEqualTo("Deleted item");
        assertThat(action.category()).isNull();
    }
}
