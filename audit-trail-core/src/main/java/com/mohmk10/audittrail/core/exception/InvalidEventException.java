package com.mohmk10.audittrail.core.exception;

import java.util.List;

public class InvalidEventException extends RuntimeException {

    private final List<String> violations;

    public InvalidEventException(String message, List<String> violations) {
        super(message);
        this.violations = violations;
    }

    public List<String> getViolations() {
        return violations;
    }
}
