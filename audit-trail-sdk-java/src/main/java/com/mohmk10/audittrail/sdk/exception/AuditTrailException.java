package com.mohmk10.audittrail.sdk.exception;

public class AuditTrailException extends RuntimeException {

    public AuditTrailException(String message) {
        super(message);
    }

    public AuditTrailException(String message, Throwable cause) {
        super(message, cause);
    }
}
