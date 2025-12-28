"""Integration tests for Audit Trail SDK"""

import pytest

from audit_trail_sdk import (
    Action,
    Actor,
    AuditTrailClient,
    Event,
    EventMetadata,
    Resource,
    SearchCriteria,
)


# Skip by default - run with: pytest -m integration
@pytest.mark.integration
class TestIntegration:
    @pytest.fixture
    def client(self) -> AuditTrailClient:
        return AuditTrailClient.builder().server_url("http://localhost:8080").build()

    def test_log_event_sync(self, client: AuditTrailClient) -> None:
        event = Event.create(
            actor=Actor.user("py-sdk-test", "Python SDK Tester"),
            action=Action.create("SDK Python integration test"),
            resource=Resource.document("doc-py-001", "Test Document"),
            metadata=EventMetadata.create("sdk-python-test", "tenant-001"),
        )

        response = client.log(event)

        assert response.id is not None
        assert response.hash is not None
        assert response.status == "STORED"

    def test_log_batch_sync(self, client: AuditTrailClient) -> None:
        events = [
            Event.create(
                actor=Actor.user("batch-user-1", "User 1"),
                action=Action.create("Batch event 1"),
                resource=Resource.document("batch-doc-1", "Doc 1"),
                metadata=EventMetadata.create("batch-test", "tenant-001"),
            ),
            Event.create(
                actor=Actor.user("batch-user-2", "User 2"),
                action=Action.update("Batch event 2"),
                resource=Resource.document("batch-doc-2", "Doc 2"),
                metadata=EventMetadata.create("batch-test", "tenant-001"),
            ),
        ]

        response = client.log_batch(events)

        assert response.total == 2
        assert response.succeeded == 2
        assert response.failed == 0

    @pytest.mark.asyncio
    async def test_log_event_async(self, client: AuditTrailClient) -> None:
        event = Event.create(
            actor=Actor.user("py-sdk-async", "Python Async Tester"),
            action=Action.create("Async test"),
            resource=Resource.document("doc-async-001", "Async Doc"),
            metadata=EventMetadata.create("sdk-python-async", "tenant-001"),
        )

        response = await client.log_async(event)

        assert response.id is not None
        assert response.status == "STORED"

    def test_get_by_id(self, client: AuditTrailClient) -> None:
        # First create an event
        event = Event.create(
            actor=Actor.user("test"),
            action=Action.read(),
            resource=Resource.document("doc-1"),
            metadata=EventMetadata.create("test", "tenant-001"),
        )
        created = client.log(event)

        # Then retrieve it
        retrieved = client.get_by_id(created.id)

        assert retrieved is not None
        assert retrieved.id == created.id

    def test_get_by_id_not_found(self, client: AuditTrailClient) -> None:
        result = client.get_by_id("non-existent-id")
        assert result is None

    def test_search(self, client: AuditTrailClient) -> None:
        # First create an event to search
        event = Event.create(
            actor=Actor.user("search-test-user", "Search Tester"),
            action=Action.create("Searchable event"),
            resource=Resource.document("search-doc-001", "Search Doc"),
            metadata=EventMetadata.create("search-test", "tenant-001"),
        )
        client.log(event)

        # Search for events
        criteria = SearchCriteria(
            tenant_id="tenant-001",
            actor_id="search-test-user",
            page=0,
            size=10,
        )
        result = client.search(criteria)

        assert result.items is not None
        assert isinstance(result.items, list)

    def test_quick_search(self, client: AuditTrailClient) -> None:
        result = client.quick_search("document", "tenant-001", 0, 10)

        assert result.items is not None
        assert isinstance(result.items, list)
