package audittrail

import (
	"context"
	"encoding/json"
	"fmt"
	"time"
)

// Client is the Audit Trail SDK client
type Client struct {
	http *HTTPClient
}

// NewClient creates a new client with the given options
func NewClient(opts ...ClientOption) (*Client, error) {
	options := defaultOptions()
	for _, opt := range opts {
		opt(options)
	}

	if options.serverURL == "" {
		return nil, NewValidationError("serverURL is required", nil)
	}

	return &Client{
		http: NewHTTPClient(options),
	}, nil
}

// Log logs a single event
func (c *Client) Log(ctx context.Context, event Event) (*EventResponse, error) {
	body, err := c.http.Post(ctx, "/api/v1/events", event)
	if err != nil {
		return nil, err
	}

	var response EventResponse
	if err := json.Unmarshal(body, &response); err != nil {
		return nil, NewConnectionError("failed to parse response", err)
	}

	return &response, nil
}

// LogBatch logs multiple events
func (c *Client) LogBatch(ctx context.Context, events []Event) (*BatchEventResponse, error) {
	payload := map[string]interface{}{
		"events": events,
	}

	body, err := c.http.Post(ctx, "/api/v1/events/batch", payload)
	if err != nil {
		return nil, err
	}

	var response BatchEventResponse
	if err := json.Unmarshal(body, &response); err != nil {
		return nil, NewConnectionError("failed to parse response", err)
	}

	return &response, nil
}

// GetByID retrieves an event by ID
func (c *Client) GetByID(ctx context.Context, id string) (*EventResponse, error) {
	body, err := c.http.Get(ctx, fmt.Sprintf("/api/v1/events/%s", id))
	if err != nil {
		// Return nil for 404
		if apiErr, ok := err.(*APIError); ok && apiErr.StatusCode == 404 {
			return nil, nil
		}
		return nil, err
	}

	var response EventResponse
	if err := json.Unmarshal(body, &response); err != nil {
		return nil, NewConnectionError("failed to parse response", err)
	}

	return &response, nil
}

// Search searches for events
func (c *Client) Search(ctx context.Context, criteria SearchCriteria) (*SearchResult, error) {
	body, err := c.http.Post(ctx, "/api/v1/search", criteria)
	if err != nil {
		return nil, err
	}

	var response SearchResult
	if err := json.Unmarshal(body, &response); err != nil {
		return nil, NewConnectionError("failed to parse response", err)
	}

	return &response, nil
}

// QuickSearch performs a quick text search
func (c *Client) QuickSearch(ctx context.Context, query, tenantID string, page, size int) (*SearchResult, error) {
	path := fmt.Sprintf("/api/v1/search/quick?q=%s&tenantId=%s&page=%d&size=%d", query, tenantID, page, size)
	body, err := c.http.Get(ctx, path)
	if err != nil {
		return nil, err
	}

	var response SearchResult
	if err := json.Unmarshal(body, &response); err != nil {
		return nil, NewConnectionError("failed to parse response", err)
	}

	return &response, nil
}

// ClientBuilder provides a fluent interface for building a Client
type ClientBuilder struct {
	opts []ClientOption
}

// NewClientBuilder creates a new ClientBuilder
func NewClientBuilder() *ClientBuilder {
	return &ClientBuilder{}
}

func (b *ClientBuilder) ServerURL(url string) *ClientBuilder {
	b.opts = append(b.opts, WithServerURL(url))
	return b
}

func (b *ClientBuilder) APIKey(key string) *ClientBuilder {
	b.opts = append(b.opts, WithAPIKey(key))
	return b
}

func (b *ClientBuilder) Timeout(seconds int) *ClientBuilder {
	b.opts = append(b.opts, WithTimeout(time.Duration(seconds)*time.Second))
	return b
}

func (b *ClientBuilder) RetryAttempts(attempts int) *ClientBuilder {
	b.opts = append(b.opts, WithRetryAttempts(attempts))
	return b
}

func (b *ClientBuilder) RetryDelay(seconds int) *ClientBuilder {
	b.opts = append(b.opts, WithRetryDelay(time.Duration(seconds)*time.Second))
	return b
}

func (b *ClientBuilder) Headers(headers map[string]string) *ClientBuilder {
	b.opts = append(b.opts, WithHeaders(headers))
	return b
}

func (b *ClientBuilder) Build() (*Client, error) {
	return NewClient(b.opts...)
}
