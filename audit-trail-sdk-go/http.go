package audittrail

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
)

// HTTPClient handles HTTP communication
type HTTPClient struct {
	client    *http.Client
	serverURL string
	apiKey    string
	headers   map[string]string
	retry     RetryConfig
}

// NewHTTPClient creates a new HTTP client
func NewHTTPClient(opts *clientOptions) *HTTPClient {
	client := opts.httpClient
	if client == nil {
		client = &http.Client{Timeout: opts.timeout}
	}

	return &HTTPClient{
		client:    client,
		serverURL: strings.TrimRight(opts.serverURL, "/"),
		apiKey:    opts.apiKey,
		headers:   opts.headers,
		retry: RetryConfig{
			MaxAttempts: opts.retryAttempts,
			Delay:       opts.retryDelay,
		},
	}
}

// Post sends a POST request
func (h *HTTPClient) Post(ctx context.Context, path string, body interface{}) ([]byte, error) {
	return RetryWithResult(ctx, h.retry, func() ([]byte, error) {
		return h.doRequest(ctx, "POST", path, body)
	})
}

// Get sends a GET request
func (h *HTTPClient) Get(ctx context.Context, path string) ([]byte, error) {
	return RetryWithResult(ctx, h.retry, func() ([]byte, error) {
		return h.doRequest(ctx, "GET", path, nil)
	})
}

func (h *HTTPClient) doRequest(ctx context.Context, method, path string, body interface{}) ([]byte, error) {
	url := h.serverURL + path

	var reqBody io.Reader
	if body != nil {
		jsonBody, err := json.Marshal(body)
		if err != nil {
			return nil, NewValidationError("failed to marshal request body", nil)
		}
		reqBody = bytes.NewBuffer(jsonBody)
	}

	req, err := http.NewRequestWithContext(ctx, method, url, reqBody)
	if err != nil {
		return nil, NewConnectionError("failed to create request", err)
	}

	// Set headers
	req.Header.Set("Content-Type", "application/json")
	if h.apiKey != "" {
		req.Header.Set("X-API-Key", h.apiKey)
	}
	for k, v := range h.headers {
		req.Header.Set(k, v)
	}

	resp, err := h.client.Do(req)
	if err != nil {
		return nil, NewConnectionError(fmt.Sprintf("request to %s failed", url), err)
	}
	defer resp.Body.Close()

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, NewConnectionError("failed to read response body", err)
	}

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return nil, NewAPIError(
			fmt.Sprintf("API request failed: %s", resp.Status),
			resp.StatusCode,
			string(respBody),
		)
	}

	return respBody, nil
}
