package com.mohmk10.audittrail.sdk.exception;

import java.util.Collections;
import java.util.List;

public class AuditTrailValidationException extends AuditTrailException {
    private final List<String> violations;

    public AuditTrailValidationException(String message) {
        super(message);
        this.violations = Collections.emptyList();
    }

    public AuditTrailValidationException(String message, List<String> violations) {
        super(message);
        this.violations = violations != null ? Collections.unmodifiableList(violations) : Collections.emptyList();
    }

    public List<String> getViolations() {
        return violations;
    }
}
