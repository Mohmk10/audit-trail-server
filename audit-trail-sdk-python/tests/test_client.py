"""Tests for Audit Trail SDK client"""

from typing import Dict

import pytest

from audit_trail_sdk import AuditTrailClient, AuditTrailClientBuilder
from audit_trail_sdk.exceptions import (
    AuditTrailApiError,
    AuditTrailConnectionError,
    AuditTrailError,
    AuditTrailValidationError,
)


class TestAuditTrailClient:
    def test_builder_creates_client(self) -> None:
        client = (
            AuditTrailClient.builder()
            .server_url("http://localhost:8080")
            .api_key("test-key")
            .timeout(10.0)
            .build()
        )
        assert client is not None

    def test_builder_requires_server_url(self) -> None:
        with pytest.raises(ValueError, match="server_url is required"):
            AuditTrailClient.builder().build()

    def test_builder_with_all_options(self) -> None:
        client = (
            AuditTrailClient.builder()
            .server_url("http://localhost:8080")
            .api_key("my-key")
            .timeout(60.0)
            .retry_attempts(5)
            .retry_delay(2.0)
            .headers({"X-Custom": "value"})
            .build()
        )
        assert client is not None

    def test_direct_instantiation(self) -> None:
        client = AuditTrailClient(
            server_url="http://localhost:8080",
            api_key="test-key",
        )
        assert client is not None


class TestAuditTrailClientBuilder:
    def test_fluent_interface(self) -> None:
        builder = AuditTrailClientBuilder()
        result = builder.server_url("http://test.com")
        assert result is builder  # Returns self for chaining

    def test_all_builder_methods_return_self(self) -> None:
        builder = AuditTrailClientBuilder()
        assert builder.server_url("http://test.com") is builder
        assert builder.api_key("key") is builder
        assert builder.timeout(30.0) is builder
        assert builder.retry_attempts(3) is builder
        assert builder.retry_delay(1.0) is builder
        assert builder.headers({}) is builder


class TestExceptions:
    def test_base_error(self) -> None:
        error = AuditTrailError("Test error")
        assert str(error) == "Test error"
        assert isinstance(error, Exception)

    def test_connection_error(self) -> None:
        cause = Exception("Network failure")
        error = AuditTrailConnectionError("Failed to connect", cause)
        assert str(error) == "Failed to connect"
        assert error.cause is cause

    def test_connection_error_without_cause(self) -> None:
        error = AuditTrailConnectionError("Failed to connect")
        assert error.cause is None

    def test_api_error(self) -> None:
        error = AuditTrailApiError("Bad request", 400, {"field": "invalid"})
        assert str(error) == "Bad request"
        assert error.status_code == 400
        assert error.body == {"field": "invalid"}

    def test_api_error_without_body(self) -> None:
        error = AuditTrailApiError("Not found", 404)
        assert error.status_code == 404
        assert error.body is None

    def test_validation_error(self) -> None:
        violations = ["Field A is required", "Field B is invalid"]
        error = AuditTrailValidationError("Validation failed", violations)
        assert str(error) == "Validation failed"
        assert error.violations == violations

    def test_validation_error_without_violations(self) -> None:
        error = AuditTrailValidationError("Validation failed")
        assert error.violations == []

    def test_error_inheritance(self) -> None:
        assert issubclass(AuditTrailConnectionError, AuditTrailError)
        assert issubclass(AuditTrailApiError, AuditTrailError)
        assert issubclass(AuditTrailValidationError, AuditTrailError)
        assert issubclass(AuditTrailError, Exception)
