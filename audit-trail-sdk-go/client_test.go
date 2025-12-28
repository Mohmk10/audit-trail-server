package audittrail

import (
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func TestNewClient(t *testing.T) {
	t.Run("with valid options", func(t *testing.T) {
		client, err := NewClient(
			WithServerURL("http://localhost:8080"),
			WithAPIKey("test-key"),
			WithTimeout(10*time.Second),
		)

		assert.NoError(t, err)
		assert.NotNil(t, client)
	})

	t.Run("without server URL", func(t *testing.T) {
		client, err := NewClient()

		assert.Error(t, err)
		assert.Nil(t, client)
		assert.True(t, IsValidationError(err))
	})

	t.Run("with all options", func(t *testing.T) {
		client, err := NewClient(
			WithServerURL("http://localhost:8080"),
			WithAPIKey("my-key"),
			WithTimeout(60*time.Second),
			WithRetryAttempts(5),
			WithRetryDelay(2*time.Second),
			WithHeaders(map[string]string{"X-Custom": "value"}),
		)

		assert.NoError(t, err)
		assert.NotNil(t, client)
	})
}

func TestClientBuilder(t *testing.T) {
	t.Run("builds client", func(t *testing.T) {
		client, err := NewClientBuilder().
			ServerURL("http://localhost:8080").
			APIKey("my-key").
			Timeout(30).
			RetryAttempts(5).
			RetryDelay(2).
			Headers(map[string]string{"X-Custom": "value"}).
			Build()

		assert.NoError(t, err)
		assert.NotNil(t, client)
	})

	t.Run("fails without server URL", func(t *testing.T) {
		client, err := NewClientBuilder().
			APIKey("key").
			Build()

		assert.Error(t, err)
		assert.Nil(t, client)
	})

	t.Run("fluent interface returns self", func(t *testing.T) {
		builder := NewClientBuilder()
		result := builder.ServerURL("http://test.com")
		assert.Equal(t, builder, result)
	})
}

func TestClientBuilderChaining(t *testing.T) {
	builder := NewClientBuilder()

	// Test that all methods return the builder for chaining
	assert.Equal(t, builder, builder.ServerURL("url"))
	assert.Equal(t, builder, builder.APIKey("key"))
	assert.Equal(t, builder, builder.Timeout(30))
	assert.Equal(t, builder, builder.RetryAttempts(3))
	assert.Equal(t, builder, builder.RetryDelay(1))
	assert.Equal(t, builder, builder.Headers(map[string]string{}))
}

func TestDefaultOptions(t *testing.T) {
	opts := defaultOptions()

	assert.Equal(t, 30*time.Second, opts.timeout)
	assert.Equal(t, 3, opts.retryAttempts)
	assert.Equal(t, 1*time.Second, opts.retryDelay)
	assert.NotNil(t, opts.headers)
	assert.Empty(t, opts.serverURL)
	assert.Empty(t, opts.apiKey)
}
