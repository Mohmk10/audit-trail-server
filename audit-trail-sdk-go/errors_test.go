package audittrail

import (
	"errors"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestConnectionError(t *testing.T) {
	cause := errors.New("network error")
	err := NewConnectionError("failed to connect", cause)

	assert.Contains(t, err.Error(), "failed to connect")
	assert.Contains(t, err.Error(), "network error")
	assert.True(t, IsConnectionError(err))
	assert.False(t, IsAPIError(err))
	assert.False(t, IsValidationError(err))
}

func TestConnectionErrorWithoutCause(t *testing.T) {
	err := NewConnectionError("failed to connect", nil)

	assert.Equal(t, "failed to connect", err.Error())
	assert.True(t, IsConnectionError(err))
}

func TestAPIError(t *testing.T) {
	err := NewAPIError("bad request", 400, `{"error":"invalid"}`)

	assert.Contains(t, err.Error(), "bad request")
	assert.Contains(t, err.Error(), "400")
	assert.Equal(t, 400, err.StatusCode)
	assert.Equal(t, `{"error":"invalid"}`, err.Body)
	assert.True(t, IsAPIError(err))
	assert.False(t, IsConnectionError(err))
	assert.False(t, IsValidationError(err))
}

func TestValidationError(t *testing.T) {
	violations := []string{"field1 required", "field2 invalid"}
	err := NewValidationError("validation failed", violations)

	assert.Equal(t, "validation failed", err.Message)
	assert.Equal(t, 2, len(err.Violations))
	assert.Equal(t, "field1 required", err.Violations[0])
	assert.Equal(t, "field2 invalid", err.Violations[1])
	assert.True(t, IsValidationError(err))
	assert.False(t, IsConnectionError(err))
	assert.False(t, IsAPIError(err))
}

func TestValidationErrorWithNilViolations(t *testing.T) {
	err := NewValidationError("validation failed", nil)
	assert.Equal(t, "validation failed", err.Message)
	assert.Nil(t, err.Violations)
}

func TestErrorUnwrap(t *testing.T) {
	cause := errors.New("root cause")
	err := NewConnectionError("wrapper", cause)

	unwrapped := errors.Unwrap(err)
	assert.Equal(t, cause, unwrapped)
}

func TestAuditTrailErrorWithCause(t *testing.T) {
	cause := errors.New("original error")
	err := &AuditTrailError{Message: "wrapped", Cause: cause}

	assert.Contains(t, err.Error(), "wrapped")
	assert.Contains(t, err.Error(), "original error")
	assert.Equal(t, cause, err.Unwrap())
}

func TestAuditTrailErrorWithoutCause(t *testing.T) {
	err := &AuditTrailError{Message: "simple error"}

	assert.Equal(t, "simple error", err.Error())
	assert.Nil(t, err.Unwrap())
}
