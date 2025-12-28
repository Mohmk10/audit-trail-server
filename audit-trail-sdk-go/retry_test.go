package audittrail

import (
	"context"
	"errors"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func TestRetry(t *testing.T) {
	t.Run("succeeds on first attempt", func(t *testing.T) {
		calls := 0
		err := Retry(context.Background(), RetryConfig{MaxAttempts: 3, Delay: 10 * time.Millisecond}, func() error {
			calls++
			return nil
		})

		assert.NoError(t, err)
		assert.Equal(t, 1, calls)
	})

	t.Run("retries on error", func(t *testing.T) {
		calls := 0
		err := Retry(context.Background(), RetryConfig{MaxAttempts: 3, Delay: 10 * time.Millisecond}, func() error {
			calls++
			if calls < 3 {
				return errors.New("temporary error")
			}
			return nil
		})

		assert.NoError(t, err)
		assert.Equal(t, 3, calls)
	})

	t.Run("fails after max attempts", func(t *testing.T) {
		calls := 0
		err := Retry(context.Background(), RetryConfig{MaxAttempts: 3, Delay: 10 * time.Millisecond}, func() error {
			calls++
			return errors.New("persistent error")
		})

		assert.Error(t, err)
		assert.Equal(t, 3, calls)
		assert.Contains(t, err.Error(), "persistent error")
	})

	t.Run("does not retry on 4xx errors", func(t *testing.T) {
		calls := 0
		err := Retry(context.Background(), RetryConfig{MaxAttempts: 3, Delay: 10 * time.Millisecond}, func() error {
			calls++
			return NewAPIError("bad request", 400, "")
		})

		assert.Error(t, err)
		assert.Equal(t, 1, calls)
		assert.True(t, IsAPIError(err))
	})

	t.Run("does not retry on validation errors", func(t *testing.T) {
		calls := 0
		err := Retry(context.Background(), RetryConfig{MaxAttempts: 3, Delay: 10 * time.Millisecond}, func() error {
			calls++
			return NewValidationError("invalid", nil)
		})

		assert.Error(t, err)
		assert.Equal(t, 1, calls)
		assert.True(t, IsValidationError(err))
	})

	t.Run("retries on 5xx errors", func(t *testing.T) {
		calls := 0
		err := Retry(context.Background(), RetryConfig{MaxAttempts: 3, Delay: 10 * time.Millisecond}, func() error {
			calls++
			if calls < 3 {
				return NewAPIError("server error", 500, "")
			}
			return nil
		})

		assert.NoError(t, err)
		assert.Equal(t, 3, calls)
	})

	t.Run("respects context cancellation", func(t *testing.T) {
		ctx, cancel := context.WithCancel(context.Background())
		cancel()

		calls := 0
		err := Retry(ctx, RetryConfig{MaxAttempts: 3, Delay: 100 * time.Millisecond}, func() error {
			calls++
			return errors.New("error")
		})

		assert.Error(t, err)
		assert.LessOrEqual(t, calls, 2)
	})
}

func TestRetryWithResult(t *testing.T) {
	t.Run("returns result on success", func(t *testing.T) {
		result, err := RetryWithResult(context.Background(), RetryConfig{MaxAttempts: 3, Delay: 10 * time.Millisecond}, func() (string, error) {
			return "success", nil
		})

		assert.NoError(t, err)
		assert.Equal(t, "success", result)
	})

	t.Run("retries and returns result", func(t *testing.T) {
		calls := 0
		result, err := RetryWithResult(context.Background(), RetryConfig{MaxAttempts: 3, Delay: 10 * time.Millisecond}, func() (int, error) {
			calls++
			if calls < 2 {
				return 0, errors.New("retry")
			}
			return 42, nil
		})

		assert.NoError(t, err)
		assert.Equal(t, 42, result)
		assert.Equal(t, 2, calls)
	})

	t.Run("returns zero value on failure", func(t *testing.T) {
		result, err := RetryWithResult(context.Background(), RetryConfig{MaxAttempts: 2, Delay: 10 * time.Millisecond}, func() (string, error) {
			return "", errors.New("error")
		})

		assert.Error(t, err)
		assert.Equal(t, "", result)
	})

	t.Run("does not retry on 4xx errors", func(t *testing.T) {
		calls := 0
		_, err := RetryWithResult(context.Background(), RetryConfig{MaxAttempts: 3, Delay: 10 * time.Millisecond}, func() (string, error) {
			calls++
			return "", NewAPIError("bad request", 404, "")
		})

		assert.Error(t, err)
		assert.Equal(t, 1, calls)
	})

	t.Run("respects context cancellation", func(t *testing.T) {
		ctx, cancel := context.WithCancel(context.Background())
		cancel()

		_, err := RetryWithResult(ctx, RetryConfig{MaxAttempts: 3, Delay: 100 * time.Millisecond}, func() (int, error) {
			return 0, errors.New("error")
		})

		assert.Error(t, err)
	})
}
