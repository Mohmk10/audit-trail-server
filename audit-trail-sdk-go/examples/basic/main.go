package main

import (
	"context"
	"fmt"
	"log"

	audittrail "github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go"
)

func main() {
	// Create client using builder
	client, err := audittrail.NewClientBuilder().
		ServerURL("http://localhost:8080").
		APIKey("your-api-key").
		Timeout(30).
		RetryAttempts(3).
		Build()
	if err != nil {
		log.Fatalf("Failed to create client: %v", err)
	}

	ctx := context.Background()

	// Example 1: Log a single event using factory functions
	event := audittrail.NewEvent(
		audittrail.NewUserActor("user-123", "John Doe"),
		audittrail.CreateAction("Created new document"),
		audittrail.DocumentResource("doc-456", "Contract.pdf"),
		audittrail.NewEventMetadata("document-service", "tenant-001"),
	)

	response, err := client.Log(ctx, event)
	if err != nil {
		log.Fatalf("Failed to log event: %v", err)
	}
	fmt.Printf("Event logged: ID=%s, Hash=%s, Status=%s\n", response.ID, response.Hash, response.Status)

	// Example 2: Log event using builders
	event2 := audittrail.NewEventBuilder().
		Actor(audittrail.NewActorBuilder().
			ID("service-001").
			Type(audittrail.ActorTypeService).
			Name("Payment Service").
			Build()).
		Action(audittrail.NewActionBuilder().
			Type(audittrail.ActionTypeUpdate).
			Description("Updated payment status").
			Build()).
		Resource(audittrail.NewResourceBuilder().
			Type("payment").
			ID("pay-789").
			Name("Payment #789").
			Build()).
		Metadata(audittrail.NewMetadataBuilder().
			Source("payment-service").
			TenantID("tenant-001").
			CorrelationID("corr-12345").
			Build()).
		Build()

	response2, err := client.Log(ctx, event2)
	if err != nil {
		log.Fatalf("Failed to log event: %v", err)
	}
	fmt.Printf("Event logged: ID=%s\n", response2.ID)

	// Example 3: Log batch events
	events := []audittrail.Event{
		audittrail.NewEvent(
			audittrail.NewUserActor("user-1", "Alice"),
			audittrail.ReadAction("Viewed report"),
			audittrail.NewResource("report", "rpt-001", "Q4 Report"),
			audittrail.NewEventMetadata("reports", "tenant-001"),
		),
		audittrail.NewEvent(
			audittrail.NewUserActor("user-2", "Bob"),
			audittrail.DeleteAction("Deleted old file"),
			audittrail.FileResource("file-002", "old-data.csv"),
			audittrail.NewEventMetadata("files", "tenant-001"),
		),
	}

	batchResponse, err := client.LogBatch(ctx, events)
	if err != nil {
		log.Fatalf("Failed to log batch: %v", err)
	}
	fmt.Printf("Batch logged: Total=%d, Succeeded=%d, Failed=%d\n",
		batchResponse.Total, batchResponse.Succeeded, batchResponse.Failed)

	// Example 4: Search events
	criteria := audittrail.SearchCriteria{
		TenantID:   "tenant-001",
		ActorID:    "user-123",
		ActionType: string(audittrail.ActionTypeCreate),
		Page:       0,
		Size:       10,
	}

	searchResult, err := client.Search(ctx, criteria)
	if err != nil {
		log.Fatalf("Failed to search: %v", err)
	}
	fmt.Printf("Found %d events (total: %d)\n", len(searchResult.Items), searchResult.TotalElements)

	// Example 5: Get event by ID
	retrieved, err := client.GetByID(ctx, response.ID)
	if err != nil {
		log.Fatalf("Failed to get event: %v", err)
	}
	if retrieved != nil {
		fmt.Printf("Retrieved event: %s\n", retrieved.ID)
	}

	// Example 6: Quick search
	quickResult, err := client.QuickSearch(ctx, "document", "tenant-001", 0, 10)
	if err != nil {
		log.Fatalf("Failed to quick search: %v", err)
	}
	fmt.Printf("Quick search found %d events\n", len(quickResult.Items))
}
