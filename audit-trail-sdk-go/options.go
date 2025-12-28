package audittrail

import (
	"net/http"
	"time"
)

// ClientOption configures the client
type ClientOption func(*clientOptions)

type clientOptions struct {
	serverURL     string
	apiKey        string
	timeout       time.Duration
	retryAttempts int
	retryDelay    time.Duration
	httpClient    *http.Client
	headers       map[string]string
}

func defaultOptions() *clientOptions {
	return &clientOptions{
		timeout:       30 * time.Second,
		retryAttempts: 3,
		retryDelay:    1 * time.Second,
		headers:       make(map[string]string),
	}
}

// WithServerURL sets the server URL
func WithServerURL(url string) ClientOption {
	return func(o *clientOptions) {
		o.serverURL = url
	}
}

// WithAPIKey sets the API key
func WithAPIKey(key string) ClientOption {
	return func(o *clientOptions) {
		o.apiKey = key
	}
}

// WithTimeout sets the HTTP timeout
func WithTimeout(timeout time.Duration) ClientOption {
	return func(o *clientOptions) {
		o.timeout = timeout
	}
}

// WithRetryAttempts sets the number of retry attempts
func WithRetryAttempts(attempts int) ClientOption {
	return func(o *clientOptions) {
		o.retryAttempts = attempts
	}
}

// WithRetryDelay sets the delay between retries
func WithRetryDelay(delay time.Duration) ClientOption {
	return func(o *clientOptions) {
		o.retryDelay = delay
	}
}

// WithHTTPClient sets a custom HTTP client
func WithHTTPClient(client *http.Client) ClientOption {
	return func(o *clientOptions) {
		o.httpClient = client
	}
}

// WithHeaders sets custom headers
func WithHeaders(headers map[string]string) ClientOption {
	return func(o *clientOptions) {
		o.headers = headers
	}
}
