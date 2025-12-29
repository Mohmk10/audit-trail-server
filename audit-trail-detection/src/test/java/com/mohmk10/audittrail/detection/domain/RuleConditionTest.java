package com.mohmk10.audittrail.detection.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleConditionTest {

    @Test
    void shouldCreateSimpleCondition() {
        RuleCondition condition = new RuleCondition("actionType", "EQUALS", "DELETE");

        assertThat(condition.getField()).isEqualTo("actionType");
        assertThat(condition.getOperator()).isEqualTo("EQUALS");
        assertThat(condition.getValue()).isEqualTo("DELETE");
    }

    @Test
    void shouldSupportEqualsOperator() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("CREATE");

        assertThat(condition.getOperator()).isEqualTo("EQUALS");
    }

    @Test
    void shouldSupportContainsOperator() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actorName");
        condition.setOperator("CONTAINS");
        condition.setValue("admin");

        assertThat(condition.getOperator()).isEqualTo("CONTAINS");
    }

    @Test
    void shouldSupportGreaterThanOperator() {
        RuleCondition condition = new RuleCondition();
        condition.setField("count");
        condition.setOperator("GREATER_THAN");
        condition.setValue(10);

        assertThat(condition.getOperator()).isEqualTo("GREATER_THAN");
    }

    @Test
    void shouldSupportInOperator() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actorIp");
        condition.setOperator("IN");
        condition.setValue(List.of("192.168.1.1", "192.168.1.2"));

        assertThat(condition.getOperator()).isEqualTo("IN");
        assertThat(condition.getValue()).isInstanceOf(List.class);
    }

    @Test
    void shouldSupportAndConditions() {
        RuleCondition condition1 = new RuleCondition("actionType", "EQUALS", "DELETE");
        RuleCondition condition2 = new RuleCondition("resourceType", "EQUALS", "DOCUMENT");

        RuleCondition andCondition = new RuleCondition();
        andCondition.setAnd(List.of(condition1, condition2));

        assertThat(andCondition.getAnd()).hasSize(2);
        assertThat(andCondition.getAnd().get(0).getField()).isEqualTo("actionType");
        assertThat(andCondition.getAnd().get(1).getField()).isEqualTo("resourceType");
    }

    @Test
    void shouldSupportOrConditions() {
        RuleCondition condition1 = new RuleCondition("actionType", "EQUALS", "DELETE");
        RuleCondition condition2 = new RuleCondition("actionType", "EQUALS", "UPDATE");

        RuleCondition orCondition = new RuleCondition();
        orCondition.setOr(List.of(condition1, condition2));

        assertThat(orCondition.getOr()).hasSize(2);
    }

    @Test
    void shouldHandleThreshold() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");
        condition.setThreshold(5);

        assertThat(condition.getThreshold()).isEqualTo(5);
    }

    @Test
    void shouldHandleWindowMinutes() {
        RuleCondition condition = new RuleCondition();
        condition.setField("actionType");
        condition.setOperator("EQUALS");
        condition.setValue("DELETE");
        condition.setWindowMinutes(60);

        assertThat(condition.getWindowMinutes()).isEqualTo(60);
    }

    @Test
    void shouldHandleNullValues() {
        RuleCondition condition = new RuleCondition();

        assertThat(condition.getField()).isNull();
        assertThat(condition.getOperator()).isNull();
        assertThat(condition.getValue()).isNull();
        assertThat(condition.getThreshold()).isNull();
        assertThat(condition.getWindowMinutes()).isNull();
        assertThat(condition.getAnd()).isNull();
        assertThat(condition.getOr()).isNull();
    }

    @Test
    void shouldSupportNestedConditions() {
        RuleCondition inner1 = new RuleCondition("actionType", "EQUALS", "DELETE");
        RuleCondition inner2 = new RuleCondition("resourceType", "EQUALS", "FILE");

        RuleCondition orCondition = new RuleCondition();
        orCondition.setOr(List.of(inner1, inner2));

        RuleCondition outer = new RuleCondition("tenantId", "EQUALS", "tenant-001");

        RuleCondition combined = new RuleCondition();
        combined.setAnd(List.of(outer, orCondition));

        assertThat(combined.getAnd()).hasSize(2);
        assertThat(combined.getAnd().get(1).getOr()).hasSize(2);
    }

    @Test
    void shouldUseDefaultConstructor() {
        RuleCondition condition = new RuleCondition();

        condition.setField("testField");
        condition.setOperator("EQUALS");
        condition.setValue("testValue");

        assertThat(condition.getField()).isEqualTo("testField");
        assertThat(condition.getOperator()).isEqualTo("EQUALS");
        assertThat(condition.getValue()).isEqualTo("testValue");
    }
}
