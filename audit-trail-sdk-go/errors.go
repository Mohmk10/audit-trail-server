package audittrail

import (
	"fmt"
)

// AuditTrailError is the base error type
type AuditTrailError struct {
	Message string
	Cause   error
}

func (e *AuditTrailError) Error() string {
	if e.Cause != nil {
		return fmt.Sprintf("%s: %v", e.Message, e.Cause)
	}
	return e.Message
}

func (e *AuditTrailError) Unwrap() error {
	return e.Cause
}

// ConnectionError represents a connection failure
type ConnectionError struct {
	AuditTrailError
}

func NewConnectionError(message string, cause error) *ConnectionError {
	return &ConnectionError{
		AuditTrailError: AuditTrailError{Message: message, Cause: cause},
	}
}

// APIError represents an API error response
type APIError struct {
	AuditTrailError
	StatusCode int
	Body       string
}

func NewAPIError(message string, statusCode int, body string) *APIError {
	return &APIError{
		AuditTrailError: AuditTrailError{Message: message},
		StatusCode:      statusCode,
		Body:            body,
	}
}

func (e *APIError) Error() string {
	return fmt.Sprintf("%s (status: %d)", e.Message, e.StatusCode)
}

// ValidationError represents a validation failure
type ValidationError struct {
	AuditTrailError
	Violations []string
}

func NewValidationError(message string, violations []string) *ValidationError {
	return &ValidationError{
		AuditTrailError: AuditTrailError{Message: message},
		Violations:      violations,
	}
}

// IsConnectionError checks if err is a ConnectionError
func IsConnectionError(err error) bool {
	_, ok := err.(*ConnectionError)
	return ok
}

// IsAPIError checks if err is an APIError
func IsAPIError(err error) bool {
	_, ok := err.(*APIError)
	return ok
}

// IsValidationError checks if err is a ValidationError
func IsValidationError(err error) bool {
	_, ok := err.(*ValidationError)
	return ok
}
