# Audit Trail SDK for Go

SDK Go pour le système d'Audit Trail Universel.

## Installation

```bash
go get github.com/mohmk10/audit-trail-server/audit-trail-sdk-go
```

## Utilisation rapide

```go
client, _ := audittrail.NewClientBuilder().ServerURL("http://localhost:8080").Build()
event := audittrail.NewEvent(audittrail.NewUserActor("user-1", "John"), audittrail.CreateAction("Created doc"), audittrail.DocumentResource("doc-1", "Contract"), audittrail.NewEventMetadata("app", "tenant-1"))
response, _ := client.Log(context.Background(), event)
```

## Configuration

### Builder Pattern

```go
client, err := audittrail.NewClientBuilder().
    ServerURL("http://localhost:8080").
    APIKey("your-api-key").
    Timeout(30).              // seconds
    RetryAttempts(3).
    RetryDelay(1).            // seconds
    Headers(map[string]string{"X-Custom": "value"}).
    Build()
```

### Functional Options

```go
client, err := audittrail.NewClient(
    audittrail.WithServerURL("http://localhost:8080"),
    audittrail.WithAPIKey("your-api-key"),
    audittrail.WithTimeout(30 * time.Second),
    audittrail.WithRetryAttempts(3),
    audittrail.WithRetryDelay(1 * time.Second),
    audittrail.WithHeaders(map[string]string{"X-Custom": "value"}),
)
```

## Création d'événements

### Factory Functions (recommandé)

```go
event := audittrail.NewEvent(
    audittrail.NewUserActor("user-123", "John Doe"),
    audittrail.CreateAction("Created document"),
    audittrail.DocumentResource("doc-456", "Contract.pdf"),
    audittrail.NewEventMetadata("my-app", "tenant-001"),
)
```

### Builders

```go
event := audittrail.NewEventBuilder().
    Actor(audittrail.NewActorBuilder().
        ID("user-123").
        Type(audittrail.ActorTypeUser).
        Name("John Doe").
        Build()).
    Action(audittrail.NewActionBuilder().
        Type(audittrail.ActionTypeCreate).
        Description("Created document").
        Build()).
    Resource(audittrail.NewResourceBuilder().
        Type("document").
        ID("doc-456").
        Name("Contract.pdf").
        Build()).
    Metadata(audittrail.NewMetadataBuilder().
        Source("my-app").
        TenantID("tenant-001").
        CorrelationID("corr-123").
        Build()).
    Build()
```

## API Client

### Log Single Event

```go
response, err := client.Log(ctx, event)
if err != nil {
    if audittrail.IsAPIError(err) {
        apiErr := err.(*audittrail.APIError)
        fmt.Printf("API error: %d - %s\n", apiErr.StatusCode, apiErr.Message)
    }
}
fmt.Printf("ID: %s, Hash: %s\n", response.ID, response.Hash)
```

### Log Batch Events

```go
events := []audittrail.Event{event1, event2, event3}
response, err := client.LogBatch(ctx, events)
fmt.Printf("Total: %d, Succeeded: %d, Failed: %d\n",
    response.Total, response.Succeeded, response.Failed)
```

### Get Event by ID

```go
event, err := client.GetByID(ctx, "event-id")
if event == nil {
    fmt.Println("Event not found")
}
```

### Search Events

```go
criteria := audittrail.SearchCriteria{
    TenantID:     "tenant-001",
    ActorID:      "user-123",
    ActionType:   "CREATE",
    ResourceType: "document",
    StartDate:    "2024-01-01T00:00:00Z",
    EndDate:      "2024-12-31T23:59:59Z",
    Page:         0,
    Size:         20,
}
result, err := client.Search(ctx, criteria)
```

### Quick Search

```go
result, err := client.QuickSearch(ctx, "document", "tenant-001", 0, 10)
```

## Types d'acteurs

| Constante | Valeur |
|-----------|--------|
| `ActorTypeUser` | "USER" |
| `ActorTypeSystem` | "SYSTEM" |
| `ActorTypeService` | "SERVICE" |

## Types d'actions

| Constante | Valeur |
|-----------|--------|
| `ActionTypeCreate` | "CREATE" |
| `ActionTypeRead` | "READ" |
| `ActionTypeUpdate` | "UPDATE" |
| `ActionTypeDelete` | "DELETE" |
| `ActionTypeLogin` | "LOGIN" |
| `ActionTypeLogout` | "LOGOUT" |
| `ActionTypeExport` | "EXPORT" |
| `ActionTypeImport` | "IMPORT" |

## Gestion des erreurs

```go
response, err := client.Log(ctx, event)
if err != nil {
    switch {
    case audittrail.IsValidationError(err):
        validErr := err.(*audittrail.ValidationError)
        fmt.Printf("Validation failed: %v\n", validErr.Violations)
    case audittrail.IsAPIError(err):
        apiErr := err.(*audittrail.APIError)
        fmt.Printf("API error %d: %s\n", apiErr.StatusCode, apiErr.Message)
    case audittrail.IsConnectionError(err):
        fmt.Println("Connection failed, check network")
    default:
        fmt.Printf("Unknown error: %v\n", err)
    }
}
```

## Context Support

Le SDK supporte entièrement les contextes Go pour l'annulation et les timeouts:

```go
ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
defer cancel()

response, err := client.Log(ctx, event)
if err != nil {
    if ctx.Err() == context.DeadlineExceeded {
        fmt.Println("Request timed out")
    }
}
```

## Tests

```bash
# Unit tests
go test -v ./...

# Integration tests (requires running server)
go test -v -tags=integration ./...
```

## License

MIT
