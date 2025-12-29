package com.mohmk10.audittrail.sdk.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTrailApiExceptionTest {

    @Test
    void shouldCreateExceptionWithStatusCodeAndMessage() {
        AuditTrailApiException exception = new AuditTrailApiException(400, "Bad request");

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo("Bad request");
        assertThat(exception.getResponseBody()).isNull();
    }

    @Test
    void shouldCreateExceptionWithStatusCodeMessageAndBody() {
        String responseBody = "{\"error\": \"Invalid input\", \"field\": \"email\"}";
        AuditTrailApiException exception = new AuditTrailApiException(422, "Validation failed", responseBody);

        assertThat(exception.getStatusCode()).isEqualTo(422);
        assertThat(exception.getMessage()).isEqualTo("Validation failed");
        assertThat(exception.getResponseBody()).isEqualTo(responseBody);
    }

    @Test
    void shouldExtendAuditTrailException() {
        AuditTrailApiException exception = new AuditTrailApiException(500, "Server error");

        assertThat(exception).isInstanceOf(AuditTrailException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 422, 429, 499})
    void shouldIdentifyClientErrors(int statusCode) {
        AuditTrailApiException exception = new AuditTrailApiException(statusCode, "Client error");

        assertThat(exception.isClientError()).isTrue();
        assertThat(exception.isServerError()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 501, 502, 503, 504, 599})
    void shouldIdentifyServerErrors(int statusCode) {
        AuditTrailApiException exception = new AuditTrailApiException(statusCode, "Server error");

        assertThat(exception.isServerError()).isTrue();
        assertThat(exception.isClientError()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 204, 299, 300, 301, 302, 399})
    void shouldNotBeClientOrServerErrorForOtherCodes(int statusCode) {
        AuditTrailApiException exception = new AuditTrailApiException(statusCode, "Not really an error");

        assertThat(exception.isClientError()).isFalse();
        assertThat(exception.isServerError()).isFalse();
    }

    @Test
    void shouldHandle400BadRequest() {
        AuditTrailApiException exception = new AuditTrailApiException(400, "Bad Request");

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.isClientError()).isTrue();
    }

    @Test
    void shouldHandle401Unauthorized() {
        AuditTrailApiException exception = new AuditTrailApiException(401, "Unauthorized");

        assertThat(exception.getStatusCode()).isEqualTo(401);
        assertThat(exception.isClientError()).isTrue();
    }

    @Test
    void shouldHandle403Forbidden() {
        AuditTrailApiException exception = new AuditTrailApiException(403, "Forbidden");

        assertThat(exception.getStatusCode()).isEqualTo(403);
        assertThat(exception.isClientError()).isTrue();
    }

    @Test
    void shouldHandle404NotFound() {
        AuditTrailApiException exception = new AuditTrailApiException(404, "Not Found");

        assertThat(exception.getStatusCode()).isEqualTo(404);
        assertThat(exception.isClientError()).isTrue();
    }

    @Test
    void shouldHandle429TooManyRequests() {
        String body = "{\"retryAfter\": 60}";
        AuditTrailApiException exception = new AuditTrailApiException(429, "Too Many Requests", body);

        assertThat(exception.getStatusCode()).isEqualTo(429);
        assertThat(exception.isClientError()).isTrue();
        assertThat(exception.getResponseBody()).contains("retryAfter");
    }

    @Test
    void shouldHandle500InternalServerError() {
        AuditTrailApiException exception = new AuditTrailApiException(500, "Internal Server Error");

        assertThat(exception.getStatusCode()).isEqualTo(500);
        assertThat(exception.isServerError()).isTrue();
    }

    @Test
    void shouldHandle502BadGateway() {
        AuditTrailApiException exception = new AuditTrailApiException(502, "Bad Gateway");

        assertThat(exception.getStatusCode()).isEqualTo(502);
        assertThat(exception.isServerError()).isTrue();
    }

    @Test
    void shouldHandle503ServiceUnavailable() {
        AuditTrailApiException exception = new AuditTrailApiException(503, "Service Unavailable");

        assertThat(exception.getStatusCode()).isEqualTo(503);
        assertThat(exception.isServerError()).isTrue();
    }

    @Test
    void shouldHandle504GatewayTimeout() {
        AuditTrailApiException exception = new AuditTrailApiException(504, "Gateway Timeout");

        assertThat(exception.getStatusCode()).isEqualTo(504);
        assertThat(exception.isServerError()).isTrue();
    }

    @Test
    void shouldStoreJsonResponseBody() {
        String jsonBody = "{\"error\": \"validation_error\", \"details\": [{\"field\": \"email\", \"message\": \"Invalid format\"}]}";
        AuditTrailApiException exception = new AuditTrailApiException(422, "Unprocessable Entity", jsonBody);

        assertThat(exception.getResponseBody()).contains("validation_error");
        assertThat(exception.getResponseBody()).contains("email");
    }

    @Test
    void shouldStoreEmptyResponseBody() {
        AuditTrailApiException exception = new AuditTrailApiException(500, "Server Error", "");

        assertThat(exception.getResponseBody()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "400, true, false",
            "404, true, false",
            "500, false, true",
            "503, false, true",
            "200, false, false"
    })
    void shouldCorrectlyClassifyErrorTypes(int statusCode, boolean isClient, boolean isServer) {
        AuditTrailApiException exception = new AuditTrailApiException(statusCode, "Test");

        assertThat(exception.isClientError()).isEqualTo(isClient);
        assertThat(exception.isServerError()).isEqualTo(isServer);
    }
}
