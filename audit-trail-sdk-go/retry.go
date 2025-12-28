package audittrail

import (
	"context"
	"time"
)

// RetryConfig holds retry configuration
type RetryConfig struct {
	MaxAttempts int
	Delay       time.Duration
}

// Retry executes fn with retry logic
func Retry(ctx context.Context, config RetryConfig, fn func() error) error {
	var lastErr error

	for attempt := 0; attempt < config.MaxAttempts; attempt++ {
		err := fn()
		if err == nil {
			return nil
		}

		lastErr = err

		// Don't retry on 4xx errors
		if apiErr, ok := err.(*APIError); ok {
			if apiErr.StatusCode >= 400 && apiErr.StatusCode < 500 {
				return err
			}
		}

		// Don't retry on validation errors
		if IsValidationError(err) {
			return err
		}

		// Wait before retry (with exponential backoff)
		if attempt < config.MaxAttempts-1 {
			delay := config.Delay * time.Duration(1<<uint(attempt))
			select {
			case <-ctx.Done():
				return ctx.Err()
			case <-time.After(delay):
			}
		}
	}

	return lastErr
}

// RetryWithResult executes fn with retry logic and returns a result
func RetryWithResult[T any](ctx context.Context, config RetryConfig, fn func() (T, error)) (T, error) {
	var lastErr error
	var zero T

	for attempt := 0; attempt < config.MaxAttempts; attempt++ {
		result, err := fn()
		if err == nil {
			return result, nil
		}

		lastErr = err

		// Don't retry on 4xx errors
		if apiErr, ok := err.(*APIError); ok {
			if apiErr.StatusCode >= 400 && apiErr.StatusCode < 500 {
				return zero, err
			}
		}

		// Don't retry on validation errors
		if IsValidationError(err) {
			return zero, err
		}

		// Wait before retry
		if attempt < config.MaxAttempts-1 {
			delay := config.Delay * time.Duration(1<<uint(attempt))
			select {
			case <-ctx.Done():
				return zero, ctx.Err()
			case <-time.After(delay):
			}
		}
	}

	return zero, lastErr
}
