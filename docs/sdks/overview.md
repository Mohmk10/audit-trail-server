# SDKs Overview

Audit Trail fournit des SDKs officiels pour les langages les plus populaires.

## SDKs disponibles

| Langage | Package | Version |
|---------|---------|---------|
| [Java](java.md) | `io.github.mohmk10:audit-trail-sdk` | 1.0.0 |
| [JavaScript/TypeScript](javascript.md) | `@mohmk10/audit-trail-sdk` | 1.0.0 |
| [Python](python.md) | `audit-trail-sdk` | 1.0.0 |
| [Go](go.md) | `github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go` | 1.0.0 |

## Fonctionnalites communes

Tous les SDKs offrent :

- **API fluide** avec pattern Builder
- **Logging synchrone et asynchrone**
- **Retry automatique** avec backoff exponentiel
- **Gestion des erreurs** typee
- **Validation** cote client
- **Connection pooling** integre

## Quick Comparison

### Installation

```java
// Java (Maven)
<dependency>
    <groupId>io.github.mohmk10</groupId>
    <artifactId>audit-trail-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

```bash
# JavaScript
npm install @mohmk10/audit-trail-sdk

# Python
pip install audit-trail-sdk

# Go
go get github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go
```

### Configuration

```java
// Java
AuditTrailClient client = AuditTrailClient.builder()
    .serverUrl("https://audit.example.com")
    .apiKey("your-api-key")
    .build();
```

```typescript
// JavaScript
const client = AuditTrailClient.builder()
  .serverUrl('https://audit.example.com')
  .apiKey('your-api-key')
  .build();
```

```python
# Python
client = AuditTrailClient.builder() \
    .server_url("https://audit.example.com") \
    .api_key("your-api-key") \
    .build()
```

```go
// Go
client, _ := audittrail.NewClientBuilder().
    ServerURL("https://audit.example.com").
    APIKey("your-api-key").
    Build()
```

### Logging d'evenement

```java
// Java
client.log(Event.builder()
    .actor(Actor.user("user-123", "John Doe"))
    .action(Action.create("Created document"))
    .resource(Resource.document("doc-456", "Q4 Report"))
    .build());
```

```typescript
// JavaScript
await client.log({
  actor: ActorBuilder.user('user-123', 'John Doe'),
  action: ActionBuilder.create('Created document'),
  resource: ResourceBuilder.document('doc-456', 'Q4 Report')
});
```

```python
# Python
client.log(Event.create(
    actor=Actor.user("user-123", "John Doe"),
    action=Action.create("Created document"),
    resource=Resource.document("doc-456", "Q4 Report")
))
```

```go
// Go
client.Log(ctx, audittrail.NewEvent(
    audittrail.NewUserActor("user-123", "John Doe"),
    audittrail.CreateAction("Created document"),
    audittrail.DocumentResource("doc-456", "Q4 Report"),
))
```

## Choisir un SDK

| Critere | Java | JavaScript | Python | Go |
|---------|------|------------|--------|-----|
| Backend | ++ | + | ++ | ++ |
| Frontend | - | ++ | - | - |
| Serverless | + | ++ | ++ | ++ |
| Performance | ++ | + | + | ++ |
| Async natif | + | ++ | ++ | ++ |

## Support

- [Issues GitHub](https://github.com/Mohmk10/audit-trail-server/issues)
- [Discussions](https://github.com/Mohmk10/audit-trail-server/discussions)
