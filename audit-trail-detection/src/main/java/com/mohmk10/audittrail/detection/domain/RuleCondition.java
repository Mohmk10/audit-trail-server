package com.mohmk10.audittrail.detection.domain;

import java.util.List;

public class RuleCondition {

    private String field;
    private String operator;
    private Object value;
    private Integer threshold;
    private Integer windowMinutes;
    private List<RuleCondition> and;
    private List<RuleCondition> or;

    public RuleCondition() {
    }

    public RuleCondition(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(Integer windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public List<RuleCondition> getAnd() {
        return and;
    }

    public void setAnd(List<RuleCondition> and) {
        this.and = and;
    }

    public List<RuleCondition> getOr() {
        return or;
    }

    public void setOr(List<RuleCondition> or) {
        this.or = or;
    }
}
