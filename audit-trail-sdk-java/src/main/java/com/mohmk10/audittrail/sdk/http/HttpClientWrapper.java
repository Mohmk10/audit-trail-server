package com.mohmk10.audittrail.sdk.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mohmk10.audittrail.sdk.config.AuditTrailConfig;
import com.mohmk10.audittrail.sdk.exception.AuditTrailApiException;
import com.mohmk10.audittrail.sdk.exception.AuditTrailConnectionException;

public class HttpClientWrapper {
    private static final Logger log = LoggerFactory.getLogger(HttpClientWrapper.class);
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String API_KEY_HEADER = "X-API-Key";

    private final HttpClient httpClient;
    private final AuditTrailConfig config;
    private final RetryPolicy retryPolicy;

    public HttpClientWrapper(AuditTrailConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(config.getConnectTimeout())
                .build();
        this.retryPolicy = new RetryPolicy(config.getMaxRetries(), config.getRetryDelay());
    }

    public String post(String path, String body) {
        return retryPolicy.execute(() -> doPost(path, body));
    }

    public CompletableFuture<String> postAsync(String path, String body) {
        return CompletableFuture.supplyAsync(() -> post(path, body));
    }

    public String get(String path) {
        return retryPolicy.execute(() -> doGet(path));
    }

    public CompletableFuture<String> getAsync(String path) {
        return CompletableFuture.supplyAsync(() -> get(path));
    }

    private String doPost(String path, String body) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(config.getServerUrl() + path))
                    .timeout(config.getReadTimeout())
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(body));

            addApiKeyHeader(requestBuilder);

            HttpRequest request = requestBuilder.build();
            log.debug("POST {} with body length {}", path, body.length());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response);
        } catch (IOException e) {
            throw new AuditTrailConnectionException("Connection failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuditTrailConnectionException("Request interrupted", e);
        }
    }

    private String doGet(String path) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(config.getServerUrl() + path))
                    .timeout(config.getReadTimeout())
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .GET();

            addApiKeyHeader(requestBuilder);

            HttpRequest request = requestBuilder.build();
            log.debug("GET {}", path);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response);
        } catch (IOException e) {
            throw new AuditTrailConnectionException("Connection failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuditTrailConnectionException("Request interrupted", e);
        }
    }

    private void addApiKeyHeader(HttpRequest.Builder requestBuilder) {
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            requestBuilder.header(API_KEY_HEADER, config.getApiKey());
        }
    }

    private String handleResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            return body;
        }

        String message = "HTTP " + statusCode + ": " + extractErrorMessage(body);
        throw new AuditTrailApiException(statusCode, message, body);
    }

    private String extractErrorMessage(String body) {
        if (body == null || body.isEmpty()) {
            return "No response body";
        }
        if (body.length() > 200) {
            return body.substring(0, 200) + "...";
        }
        return body;
    }
}
