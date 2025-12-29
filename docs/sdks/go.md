# SDK Go

Guide complet du SDK Go pour Audit Trail.

## Installation

```bash
go get github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go
```

## Configuration

### Configuration basique

```go
package main

import (
    "context"
    audittrail "github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go"
)

func main() {
    client, err := audittrail.NewClientBuilder().
        ServerURL("https://audit.example.com").
        APIKey("your-api-key").
        Build()
    if err != nil {
        log.Fatal(err)
    }
    defer client.Close()
}
```

### Configuration avancee

```go
client, err := audittrail.NewClientBuilder().
    ServerURL("https://audit.example.com").
    APIKey("your-api-key").
    Timeout(30 * time.Second).
    RetryAttempts(3).
    RetryDelay(500 * time.Millisecond).
    ConnectionPoolSize(10).
    Build()
```

## Logging d'evenements

### Synchrone

```go
ctx := context.Background()

response, err := client.Log(ctx, audittrail.NewEvent(
    audittrail.NewUserActor("user-123", "John Doe"),
    audittrail.CreateAction("Created document"),
    audittrail.DocumentResource("doc-456", "Q4 Report"),
    audittrail.WithMetadata(audittrail.EventMetadata{
        Source:        "web-app",
        TenantID:      "tenant-001",
        CorrelationID: "req-789",
    }),
))
if err != nil {
    log.Printf("Failed to log event: %v", err)
    return
}

fmt.Printf("Event ID: %s\n", response.ID)
fmt.Printf("Hash: %s\n", response.Hash)
```

### Asynchrone (goroutine)

```go
// Fire-and-forget avec goroutine
go func() {
    _, err := client.Log(ctx, audittrail.NewEvent(
        audittrail.NewUserActor("user-123", "John Doe"),
        audittrail.ReadAction("Viewed document"),
        audittrail.DocumentResource("doc-456", "Q4 Report"),
    ))
    if err != nil {
        log.Printf("Async log failed: %v", err)
    }
}()
```

### Avec channel

```go
type LogResult struct {
    Response *audittrail.EventResponse
    Err      error
}

func logAsync(client *audittrail.Client, event *audittrail.Event) <-chan LogResult {
    resultCh := make(chan LogResult, 1)
    go func() {
        resp, err := client.Log(context.Background(), event)
        resultCh <- LogResult{Response: resp, Err: err}
    }()
    return resultCh
}

// Utilisation
resultCh := logAsync(client, event)
// ... faire autre chose ...
result := <-resultCh
if result.Err != nil {
    log.Printf("Error: %v", result.Err)
}
```

### Batch

```go
events := []*audittrail.Event{event1, event2, event3}
response, err := client.LogBatch(ctx, events)
if err != nil {
    log.Fatal(err)
}

fmt.Printf("Total: %d\n", response.Total)
fmt.Printf("Succeeded: %d\n", response.Succeeded)
fmt.Printf("Failed: %d\n", response.Failed)

for _, e := range response.Errors {
    fmt.Printf("Error at index %d: %s\n", e.Index, e.Message)
}
```

## Builders

### Actor

```go
// Utilisateur
user := audittrail.NewUserActor("user-123", "John Doe")

// Systeme
system := audittrail.NewSystemActor("batch-processor")

// Service
service := audittrail.NewServiceActor("api-gateway", "API Gateway")

// Avec options
userWithOptions := audittrail.NewActorBuilder().
    ID("user-123").
    Type(audittrail.ActorTypeUser).
    Name("John Doe").
    IP("192.168.1.100").
    UserAgent("Mozilla/5.0...").
    Attribute("department", "Engineering").
    Attribute("role", "Developer").
    Build()
```

### Action

```go
// Actions predefinies
create := audittrail.CreateAction("Created document")
read := audittrail.ReadAction("Viewed document")
update := audittrail.UpdateAction("Updated document")
del := audittrail.DeleteAction("Deleted document")
login := audittrail.LoginAction()
logout := audittrail.LogoutAction()

// Action personnalisee
custom := audittrail.NewAction("APPROVE", "Approved request", "WORKFLOW")
```

### Resource

```go
// Types predefinis
doc := audittrail.DocumentResource("doc-456", "Q4 Report")
user := audittrail.UserResource("user-789", "Jane Smith")
txn := audittrail.TransactionResource("txn-123", "Payment #456")

// Avec changements
withChanges := audittrail.NewResourceBuilder().
    ID("doc-456").
    Type(audittrail.ResourceTypeDocument).
    Name("Q4 Report").
    Before(map[string]interface{}{"status": "draft", "version": 1}).
    After(map[string]interface{}{"status": "published", "version": 2}).
    Build()
```

### Metadata

```go
metadata := audittrail.EventMetadata{
    Source:        "web-app",
    TenantID:      "tenant-001",
    CorrelationID: "req-789",
    SessionID:     "sess-abc",
    Tags: map[string]string{
        "priority":    "high",
        "environment": "production",
    },
}
```

## Gestion des erreurs

```go
import "errors"

response, err := client.Log(ctx, event)
if err != nil {
    var connErr *audittrail.ConnectionError
    var apiErr *audittrail.APIError
    var validErr *audittrail.ValidationError

    switch {
    case errors.As(err, &connErr):
        // Erreur de connexion (reseau, timeout)
        log.Printf("Connection failed: %v", connErr)
        // Retry ou fallback

    case errors.As(err, &apiErr):
        // Erreur API (4xx, 5xx)
        log.Printf("API error %d: %s", apiErr.StatusCode, apiErr.Message)
        if apiErr.StatusCode == 429 {
            // Rate limited - attendre et reessayer
        }

    case errors.As(err, &validErr):
        // Erreur de validation
        log.Printf("Validation failed: %v", validErr.Violations)
        // Corriger les donnees

    default:
        log.Printf("Unknown error: %v", err)
    }
}
```

## Integration HTTP Handler

### Middleware

```go
package middleware

import (
    "net/http"
    audittrail "github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go"
)

func AuditMiddleware(client *audittrail.Client) func(http.Handler) http.Handler {
    return func(next http.Handler) http.Handler {
        return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
            // Wrapper pour capturer le status code
            wrapped := &responseWriter{ResponseWriter: w, statusCode: 200}
            next.ServeHTTP(wrapped, r)

            // Log async apres la reponse
            go func() {
                user := getUserFromContext(r.Context())
                _, _ = client.Log(r.Context(), audittrail.NewEvent(
                    audittrail.NewUserActor(user.ID, user.Name),
                    audittrail.NewAction(r.Method, r.Method+" "+r.URL.Path, "API"),
                    audittrail.NewResource(r.URL.Path, "ENDPOINT", r.URL.Path),
                    audittrail.WithMetadata(audittrail.EventMetadata{
                        Source:        "api-server",
                        TenantID:      r.Header.Get("X-Tenant-ID"),
                        CorrelationID: r.Header.Get("X-Correlation-ID"),
                    }),
                ))
            }()
        })
    }
}

type responseWriter struct {
    http.ResponseWriter
    statusCode int
}

func (w *responseWriter) WriteHeader(code int) {
    w.statusCode = code
    w.ResponseWriter.WriteHeader(code)
}
```

### Utilisation

```go
func main() {
    client, _ := audittrail.NewClientBuilder().
        ServerURL(os.Getenv("AUDIT_TRAIL_URL")).
        APIKey(os.Getenv("AUDIT_TRAIL_API_KEY")).
        Build()
    defer client.Close()

    mux := http.NewServeMux()
    mux.HandleFunc("/api/documents", handleDocuments)

    handler := middleware.AuditMiddleware(client)(mux)
    http.ListenAndServe(":8080", handler)
}
```

## Integration Gin

```go
package main

import (
    "github.com/gin-gonic/gin"
    audittrail "github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go"
)

func AuditMiddleware(client *audittrail.Client) gin.HandlerFunc {
    return func(c *gin.Context) {
        c.Next()

        go func() {
            user, _ := c.Get("user")
            u := user.(*User)

            _, _ = client.Log(c.Request.Context(), audittrail.NewEvent(
                audittrail.NewUserActor(u.ID, u.Name),
                audittrail.NewAction(c.Request.Method, c.Request.Method+" "+c.FullPath(), "API"),
                audittrail.NewResource(c.Param("id"), "RESOURCE", c.FullPath()),
                audittrail.WithMetadata(audittrail.EventMetadata{
                    Source:   "gin-app",
                    TenantID: c.GetHeader("X-Tenant-ID"),
                }),
            ))
        }()
    }
}

func main() {
    client, _ := audittrail.NewClientBuilder().
        ServerURL(os.Getenv("AUDIT_TRAIL_URL")).
        APIKey(os.Getenv("AUDIT_TRAIL_API_KEY")).
        Build()
    defer client.Close()

    r := gin.Default()
    r.Use(AuditMiddleware(client))

    r.GET("/documents/:id", func(c *gin.Context) {
        // ...
    })

    r.Run(":8080")
}
```

## Integration Echo

```go
package main

import (
    "github.com/labstack/echo/v4"
    audittrail "github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go"
)

func AuditMiddleware(client *audittrail.Client) echo.MiddlewareFunc {
    return func(next echo.HandlerFunc) echo.HandlerFunc {
        return func(c echo.Context) error {
            err := next(c)

            go func() {
                user := c.Get("user").(*User)
                _, _ = client.Log(c.Request().Context(), audittrail.NewEvent(
                    audittrail.NewUserActor(user.ID, user.Name),
                    audittrail.NewAction(c.Request().Method, c.Request().Method+" "+c.Path(), "API"),
                    audittrail.NewResource(c.Param("id"), "RESOURCE", c.Path()),
                ))
            }()

            return err
        }
    }
}

func main() {
    client, _ := audittrail.NewClientBuilder().
        ServerURL(os.Getenv("AUDIT_TRAIL_URL")).
        APIKey(os.Getenv("AUDIT_TRAIL_API_KEY")).
        Build()
    defer client.Close()

    e := echo.New()
    e.Use(AuditMiddleware(client))

    e.GET("/documents/:id", func(c echo.Context) error {
        // ...
        return c.JSON(200, doc)
    })

    e.Start(":8080")
}
```

## Integration gRPC

```go
package interceptor

import (
    "context"
    "google.golang.org/grpc"
    audittrail "github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go"
)

func AuditUnaryInterceptor(client *audittrail.Client) grpc.UnaryServerInterceptor {
    return func(
        ctx context.Context,
        req interface{},
        info *grpc.UnaryServerInfo,
        handler grpc.UnaryHandler,
    ) (interface{}, error) {
        resp, err := handler(ctx, req)

        go func() {
            user := getUserFromContext(ctx)
            _, _ = client.Log(ctx, audittrail.NewEvent(
                audittrail.NewUserActor(user.ID, user.Name),
                audittrail.NewAction("GRPC_CALL", info.FullMethod, "GRPC"),
                audittrail.NewResource(info.FullMethod, "GRPC_METHOD", info.FullMethod),
            ))
        }()

        return resp, err
    }
}

// Utilisation
server := grpc.NewServer(
    grpc.UnaryInterceptor(interceptor.AuditUnaryInterceptor(client)),
)
```

## Options fonctionnelles

```go
// Pattern d'options fonctionnelles
event := audittrail.NewEvent(
    audittrail.NewUserActor("user-123", "John Doe"),
    audittrail.CreateAction("Created document"),
    audittrail.DocumentResource("doc-456", "Q4 Report"),
    audittrail.WithMetadata(audittrail.EventMetadata{
        Source:   "web-app",
        TenantID: "tenant-001",
    }),
    audittrail.WithCorrelationID("req-789"),
    audittrail.WithSessionID("sess-abc"),
    audittrail.WithTag("priority", "high"),
)
```

## Context et annulation

```go
// Avec timeout
ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
defer cancel()

response, err := client.Log(ctx, event)
if err != nil {
    if errors.Is(err, context.DeadlineExceeded) {
        log.Println("Request timed out")
    }
}

// Avec annulation
ctx, cancel := context.WithCancel(context.Background())

go func() {
    time.Sleep(2 * time.Second)
    cancel() // Annuler apres 2 secondes
}()

response, err := client.Log(ctx, event)
```

## Pool de workers

```go
type AuditWorkerPool struct {
    client   *audittrail.Client
    eventCh  chan *audittrail.Event
    workerWg sync.WaitGroup
}

func NewAuditWorkerPool(client *audittrail.Client, workers int) *AuditWorkerPool {
    pool := &AuditWorkerPool{
        client:  client,
        eventCh: make(chan *audittrail.Event, 1000),
    }

    for i := 0; i < workers; i++ {
        pool.workerWg.Add(1)
        go pool.worker()
    }

    return pool
}

func (p *AuditWorkerPool) worker() {
    defer p.workerWg.Done()
    for event := range p.eventCh {
        ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
        _, err := p.client.Log(ctx, event)
        cancel()
        if err != nil {
            log.Printf("Failed to log event: %v", err)
        }
    }
}

func (p *AuditWorkerPool) Log(event *audittrail.Event) {
    p.eventCh <- event
}

func (p *AuditWorkerPool) Close() {
    close(p.eventCh)
    p.workerWg.Wait()
}

// Utilisation
pool := NewAuditWorkerPool(client, 10)
defer pool.Close()

pool.Log(event) // Non-bloquant
```

## Bonnes pratiques

1. **Utilisez `defer client.Close()`** pour liberer les ressources
2. **Utilisez des goroutines** pour le logging non-bloquant
3. **Gerez le context** pour les timeouts et annulations
4. **Implementez un worker pool** pour les applications a haut volume
5. **Utilisez les options fonctionnelles** pour une API flexible
