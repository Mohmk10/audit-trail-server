//go:build integration
// +build integration

package audittrail

import (
	"context"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestIntegration(t *testing.T) {
	client, err := NewClientBuilder().
		ServerURL("http://localhost:8080").
		Build()
	assert.NoError(t, err)

	ctx := context.Background()

	t.Run("Log event", func(t *testing.T) {
		event := NewEvent(
			NewUserActor("go-sdk-test", "Go SDK Tester"),
			CreateAction("SDK Go integration test"),
			DocumentResource("doc-go-001", "Test Document"),
			NewEventMetadata("sdk-go-test", "tenant-001"),
		)

		response, err := client.Log(ctx, event)

		assert.NoError(t, err)
		assert.NotEmpty(t, response.ID)
		assert.NotEmpty(t, response.Hash)
		assert.Equal(t, "STORED", response.Status)
	})

	t.Run("Log batch events", func(t *testing.T) {
		events := []Event{
			NewEvent(
				NewUserActor("user-1", "User 1"),
				CreateAction("Batch test 1"),
				DocumentResource("doc-1", "Doc 1"),
				NewEventMetadata("test", "tenant-001"),
			),
			NewEvent(
				NewUserActor("user-2", "User 2"),
				UpdateAction("Batch test 2"),
				DocumentResource("doc-2", "Doc 2"),
				NewEventMetadata("test", "tenant-001"),
			),
		}

		response, err := client.LogBatch(ctx, events)

		assert.NoError(t, err)
		assert.Equal(t, 2, response.Total)
		assert.Equal(t, 2, response.Succeeded)
	})

	t.Run("Get by ID", func(t *testing.T) {
		// Create event first
		event := NewEvent(
			NewUserActor("test", "Test"),
			ReadAction("Get test"),
			DocumentResource("doc-get", "Doc"),
			NewEventMetadata("test", "tenant-001"),
		)
		created, err := client.Log(ctx, event)
		assert.NoError(t, err)

		// Retrieve it
		retrieved, err := client.GetByID(ctx, created.ID)

		assert.NoError(t, err)
		assert.NotNil(t, retrieved)
		assert.Equal(t, created.ID, retrieved.ID)
	})

	t.Run("Get non-existent returns nil", func(t *testing.T) {
		result, err := client.GetByID(ctx, "non-existent-id")

		assert.NoError(t, err)
		assert.Nil(t, result)
	})

	t.Run("Search events", func(t *testing.T) {
		criteria := SearchCriteria{
			TenantID: "tenant-001",
			Page:     0,
			Size:     10,
		}

		result, err := client.Search(ctx, criteria)

		assert.NoError(t, err)
		assert.NotNil(t, result)
		assert.NotNil(t, result.Items)
	})

	t.Run("Quick search", func(t *testing.T) {
		result, err := client.QuickSearch(ctx, "document", "tenant-001", 0, 10)

		assert.NoError(t, err)
		assert.NotNil(t, result)
		assert.NotNil(t, result.Items)
	})
}
