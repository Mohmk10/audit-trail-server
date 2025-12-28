package com.mohmk10.audittrail.sdk.exception;

public class AuditTrailApiException extends AuditTrailException {
    private final int statusCode;
    private final String responseBody;

    public AuditTrailApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = null;
    }

    public AuditTrailApiException(int statusCode, String message, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }
}
