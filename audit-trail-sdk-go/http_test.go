package audittrail

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func TestHTTPClient(t *testing.T) {
	t.Run("Post success", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "POST", r.Method)
			assert.Equal(t, "application/json", r.Header.Get("Content-Type"))
			w.WriteHeader(http.StatusOK)
			json.NewEncoder(w).Encode(map[string]string{"status": "ok"})
		}))
		defer server.Close()

		opts := &clientOptions{
			serverURL:     server.URL,
			timeout:       5 * time.Second,
			retryAttempts: 1,
			retryDelay:    10 * time.Millisecond,
			headers:       make(map[string]string),
		}
		client := NewHTTPClient(opts)

		body, err := client.Post(context.Background(), "/test", map[string]string{"key": "value"})

		assert.NoError(t, err)
		assert.Contains(t, string(body), "ok")
	})

	t.Run("Get success", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "GET", r.Method)
			w.WriteHeader(http.StatusOK)
			json.NewEncoder(w).Encode(map[string]string{"data": "test"})
		}))
		defer server.Close()

		opts := &clientOptions{
			serverURL:     server.URL,
			timeout:       5 * time.Second,
			retryAttempts: 1,
			retryDelay:    10 * time.Millisecond,
			headers:       make(map[string]string),
		}
		client := NewHTTPClient(opts)

		body, err := client.Get(context.Background(), "/test")

		assert.NoError(t, err)
		assert.Contains(t, string(body), "test")
	})

	t.Run("handles API error", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusBadRequest)
			w.Write([]byte(`{"error":"invalid"}`))
		}))
		defer server.Close()

		opts := &clientOptions{
			serverURL:     server.URL,
			timeout:       5 * time.Second,
			retryAttempts: 1,
			retryDelay:    10 * time.Millisecond,
			headers:       make(map[string]string),
		}
		client := NewHTTPClient(opts)

		_, err := client.Post(context.Background(), "/test", nil)

		assert.Error(t, err)
		assert.True(t, IsAPIError(err))
		apiErr := err.(*APIError)
		assert.Equal(t, 400, apiErr.StatusCode)
	})

	t.Run("includes API key header", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "test-api-key", r.Header.Get("X-API-Key"))
			w.WriteHeader(http.StatusOK)
			w.Write([]byte(`{}`))
		}))
		defer server.Close()

		opts := &clientOptions{
			serverURL:     server.URL,
			apiKey:        "test-api-key",
			timeout:       5 * time.Second,
			retryAttempts: 1,
			retryDelay:    10 * time.Millisecond,
			headers:       make(map[string]string),
		}
		client := NewHTTPClient(opts)

		_, err := client.Get(context.Background(), "/test")

		assert.NoError(t, err)
	})

	t.Run("includes custom headers", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "custom-value", r.Header.Get("X-Custom-Header"))
			w.WriteHeader(http.StatusOK)
			w.Write([]byte(`{}`))
		}))
		defer server.Close()

		opts := &clientOptions{
			serverURL:     server.URL,
			timeout:       5 * time.Second,
			retryAttempts: 1,
			retryDelay:    10 * time.Millisecond,
			headers:       map[string]string{"X-Custom-Header": "custom-value"},
		}
		client := NewHTTPClient(opts)

		_, err := client.Get(context.Background(), "/test")

		assert.NoError(t, err)
	})

	t.Run("handles 404 error", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusNotFound)
			w.Write([]byte(`{"error":"not found"}`))
		}))
		defer server.Close()

		opts := &clientOptions{
			serverURL:     server.URL,
			timeout:       5 * time.Second,
			retryAttempts: 1,
			retryDelay:    10 * time.Millisecond,
			headers:       make(map[string]string),
		}
		client := NewHTTPClient(opts)

		_, err := client.Get(context.Background(), "/test")

		assert.Error(t, err)
		assert.True(t, IsAPIError(err))
		apiErr := err.(*APIError)
		assert.Equal(t, 404, apiErr.StatusCode)
	})

	t.Run("handles server error with retry", func(t *testing.T) {
		calls := 0
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			calls++
			if calls < 3 {
				w.WriteHeader(http.StatusInternalServerError)
				w.Write([]byte(`{"error":"server error"}`))
				return
			}
			w.WriteHeader(http.StatusOK)
			w.Write([]byte(`{"status":"ok"}`))
		}))
		defer server.Close()

		opts := &clientOptions{
			serverURL:     server.URL,
			timeout:       5 * time.Second,
			retryAttempts: 3,
			retryDelay:    10 * time.Millisecond,
			headers:       make(map[string]string),
		}
		client := NewHTTPClient(opts)

		body, err := client.Get(context.Background(), "/test")

		assert.NoError(t, err)
		assert.Contains(t, string(body), "ok")
		assert.Equal(t, 3, calls)
	})

	t.Run("trims trailing slash from server URL", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/api/test", r.URL.Path)
			w.WriteHeader(http.StatusOK)
			w.Write([]byte(`{}`))
		}))
		defer server.Close()

		opts := &clientOptions{
			serverURL:     server.URL + "/",
			timeout:       5 * time.Second,
			retryAttempts: 1,
			retryDelay:    10 * time.Millisecond,
			headers:       make(map[string]string),
		}
		client := NewHTTPClient(opts)

		_, err := client.Get(context.Background(), "/api/test")

		assert.NoError(t, err)
	})
}
