"""Tests for Audit Trail SDK models"""

from typing import Dict

import pytest

from audit_trail_sdk import (
    Action,
    Actor,
    ActorType,
    Event,
    EventMetadata,
    Resource,
)


class TestActor:
    def test_user_factory(self) -> None:
        actor = Actor.user("user-123", "John Doe")
        assert actor.id == "user-123"
        assert actor.type == ActorType.USER
        assert actor.name == "John Doe"

    def test_user_factory_without_name(self) -> None:
        actor = Actor.user("user-123")
        assert actor.id == "user-123"
        assert actor.type == ActorType.USER
        assert actor.name is None

    def test_system_factory(self) -> None:
        actor = Actor.system("sys-001")
        assert actor.id == "sys-001"
        assert actor.type == ActorType.SYSTEM

    def test_service_factory(self) -> None:
        actor = Actor.service("svc-001", "Payment Service")
        assert actor.id == "svc-001"
        assert actor.type == ActorType.SERVICE
        assert actor.name == "Payment Service"

    def test_actor_with_attributes(self) -> None:
        actor = Actor(
            id="user-1",
            type=ActorType.USER,
            ip="192.168.1.1",
            user_agent="Mozilla/5.0",
            attributes={"department": "IT"},
        )
        assert actor.ip == "192.168.1.1"
        assert actor.user_agent == "Mozilla/5.0"
        assert actor.attributes == {"department": "IT"}

    def test_actor_serialization(self) -> None:
        actor = Actor(id="user-1", type=ActorType.USER, user_agent="Test Agent")
        data = actor.model_dump(by_alias=True)
        assert data["userAgent"] == "Test Agent"


class TestAction:
    def test_create_factory(self) -> None:
        action = Action.create("Created document")
        assert action.type == "CREATE"
        assert action.description == "Created document"

    def test_create_factory_without_description(self) -> None:
        action = Action.create()
        assert action.type == "CREATE"
        assert action.description is None

    def test_read_factory(self) -> None:
        action = Action.read("Viewed document")
        assert action.type == "READ"
        assert action.description == "Viewed document"

    def test_update_factory(self) -> None:
        action = Action.update("Updated record")
        assert action.type == "UPDATE"

    def test_delete_factory(self) -> None:
        action = Action.delete()
        assert action.type == "DELETE"

    def test_login_factory(self) -> None:
        action = Action.login()
        assert action.type == "LOGIN"
        assert action.description == "User login"

    def test_logout_factory(self) -> None:
        action = Action.logout()
        assert action.type == "LOGOUT"
        assert action.description == "User logout"

    def test_custom_action(self) -> None:
        action = Action.of("APPROVE", "Approved request", "WORKFLOW")
        assert action.type == "APPROVE"
        assert action.description == "Approved request"
        assert action.category == "WORKFLOW"


class TestResource:
    def test_document_factory(self) -> None:
        resource = Resource.document("doc-123", "Annual Report")
        assert resource.id == "doc-123"
        assert resource.type == "DOCUMENT"
        assert resource.name == "Annual Report"

    def test_user_factory(self) -> None:
        resource = Resource.user("user-456", "John Doe")
        assert resource.id == "user-456"
        assert resource.type == "USER"

    def test_transaction_factory(self) -> None:
        resource = Resource.transaction("txn-789", "Payment")
        assert resource.id == "txn-789"
        assert resource.type == "TRANSACTION"

    def test_custom_resource(self) -> None:
        resource = Resource.of("config-1", "CONFIG", "App Settings")
        assert resource.id == "config-1"
        assert resource.type == "CONFIG"
        assert resource.name == "App Settings"

    def test_with_before_after(self) -> None:
        resource = Resource.document("doc-1", "Doc")
        resource = resource.with_before({"status": "draft"})
        resource = resource.with_after({"status": "published"})
        assert resource.before == {"status": "draft"}
        assert resource.after == {"status": "published"}


class TestEventMetadata:
    def test_create_metadata(self) -> None:
        metadata = EventMetadata.create(
            source="web-app",
            tenant_id="tenant-001",
            correlation_id="corr-123",
        )
        assert metadata.source == "web-app"
        assert metadata.tenant_id == "tenant-001"
        assert metadata.correlation_id == "corr-123"

    def test_create_metadata_minimal(self) -> None:
        metadata = EventMetadata.create("web-app", "tenant-001")
        assert metadata.source == "web-app"
        assert metadata.tenant_id == "tenant-001"
        assert metadata.correlation_id is None
        assert metadata.session_id is None

    def test_metadata_with_tags(self) -> None:
        metadata = EventMetadata.create(
            source="app",
            tenant_id="t1",
            tags={"env": "production", "region": "us-east"},
        )
        assert metadata.tags == {"env": "production", "region": "us-east"}

    def test_metadata_serialization(self) -> None:
        metadata = EventMetadata.create(
            source="app",
            tenant_id="tenant-001",
            correlation_id="corr-123",
        )
        data = metadata.model_dump(by_alias=True)
        assert data["tenantId"] == "tenant-001"
        assert data["correlationId"] == "corr-123"


class TestEvent:
    def test_create_event(self) -> None:
        event = Event.create(
            actor=Actor.user("user-1", "John"),
            action=Action.create("Created"),
            resource=Resource.document("doc-1", "Report"),
            metadata=EventMetadata.create("app", "tenant-1"),
        )
        assert event.actor.id == "user-1"
        assert event.action.type == "CREATE"
        assert event.resource.type == "DOCUMENT"
        assert event.metadata.tenant_id == "tenant-1"

    def test_event_serialization(self) -> None:
        event = Event.create(
            actor=Actor.user("user-1"),
            action=Action.create(),
            resource=Resource.document("doc-1"),
            metadata=EventMetadata.create("app", "tenant-1"),
        )
        data = event.model_dump(by_alias=True)
        assert data["metadata"]["tenantId"] == "tenant-1"
        assert data["actor"]["type"] == "USER"

    def test_event_full_serialization(self) -> None:
        event = Event.create(
            actor=Actor(
                id="user-1",
                type=ActorType.USER,
                name="John",
                user_agent="Chrome",
            ),
            action=Action.of("APPROVE", "Approved doc", "WORKFLOW"),
            resource=Resource.document("doc-1", "Report").with_before(
                {"status": "pending"}
            ).with_after({"status": "approved"}),
            metadata=EventMetadata.create(
                source="app",
                tenant_id="tenant-1",
                correlation_id="corr-123",
                session_id="sess-456",
            ),
        )
        data = event.model_dump(by_alias=True)

        assert data["actor"]["userAgent"] == "Chrome"
        assert data["action"]["category"] == "WORKFLOW"
        assert data["resource"]["before"] == {"status": "pending"}
        assert data["resource"]["after"] == {"status": "approved"}
        assert data["metadata"]["correlationId"] == "corr-123"
        assert data["metadata"]["sessionId"] == "sess-456"
