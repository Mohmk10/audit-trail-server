# SDK Java

Guide complet du SDK Java pour Audit Trail.

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.mohmk10</groupId>
    <artifactId>audit-trail-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.mohmk10:audit-trail-sdk:1.0.0'
```

## Configuration

### Configuration basique

```java
AuditTrailClient client = AuditTrailClient.builder()
    .serverUrl("https://audit.example.com")
    .apiKey("your-api-key")
    .build();
```

### Configuration avancee

```java
AuditTrailClient client = AuditTrailClient.builder()
    .serverUrl("https://audit.example.com")
    .apiKey("your-api-key")
    .timeout(Duration.ofSeconds(30))
    .retryAttempts(3)
    .retryDelay(Duration.ofMillis(500))
    .connectionPoolSize(10)
    .build();
```

## Logging d'evenements

### Synchrone

```java
EventResponse response = client.log(Event.builder()
    .actor(Actor.user("user-123", "John Doe"))
    .action(Action.create("Created document"))
    .resource(Resource.document("doc-456", "Q4 Report"))
    .metadata(EventMetadata.builder()
        .source("web-app")
        .tenantId("tenant-001")
        .correlationId("req-789")
        .build())
    .build());

System.out.println("Event ID: " + response.id());
System.out.println("Hash: " + response.hash());
```

### Asynchrone

```java
CompletableFuture<EventResponse> future = client.logAsync(Event.builder()
    .actor(Actor.user("user-123", "John Doe"))
    .action(Action.create("Created document"))
    .resource(Resource.document("doc-456", "Q4 Report"))
    .build());

future.thenAccept(response -> {
    System.out.println("Event logged: " + response.id());
}).exceptionally(ex -> {
    System.err.println("Failed: " + ex.getMessage());
    return null;
});
```

### Batch

```java
List<Event> events = List.of(event1, event2, event3);
BatchEventResponse response = client.logBatch(events);

System.out.println("Total: " + response.total());
System.out.println("Succeeded: " + response.succeeded());
System.out.println("Failed: " + response.failed());

response.errors().forEach(error -> {
    System.err.println("Error at index " + error.index() + ": " + error.message());
});
```

## Builders

### Actor

```java
// Utilisateur
Actor user = Actor.user("user-123", "John Doe");

// Systeme
Actor system = Actor.system("batch-processor");

// Service
Actor service = Actor.service("api-gateway", "API Gateway");

// Avec attributs
Actor userWithAttrs = Actor.builder()
    .id("user-123")
    .type(ActorType.USER)
    .name("John Doe")
    .ip("192.168.1.100")
    .userAgent("Mozilla/5.0...")
    .attribute("department", "Engineering")
    .attribute("role", "Developer")
    .build();
```

### Action

```java
// Actions predefinies
Action create = Action.create("Created document");
Action read = Action.read("Viewed document");
Action update = Action.update("Updated document");
Action delete = Action.delete("Deleted document");
Action login = Action.login();
Action logout = Action.logout();

// Action personnalisee
Action custom = Action.of("APPROVE", "Approved request", "WORKFLOW");
```

### Resource

```java
// Types predefinis
Resource doc = Resource.document("doc-456", "Q4 Report");
Resource user = Resource.user("user-789", "Jane Smith");
Resource txn = Resource.transaction("txn-123", "Payment #456");

// Avec changements
Resource withChanges = Resource.builder()
    .id("doc-456")
    .type(ResourceType.DOCUMENT)
    .name("Q4 Report")
    .before(Map.of("status", "draft", "version", 1))
    .after(Map.of("status", "published", "version", 2))
    .build();
```

### Metadata

```java
EventMetadata metadata = EventMetadata.builder()
    .source("web-app")
    .tenantId("tenant-001")
    .correlationId("req-789")
    .sessionId("sess-abc")
    .tag("priority", "high")
    .tag("environment", "production")
    .build();
```

## Recuperation d'evenements

```java
// Par ID
Optional<EventResponse> event = client.findById("550e8400-...");

event.ifPresent(e -> {
    System.out.println("Found: " + e.id());
    System.out.println("Timestamp: " + e.timestamp());
    System.out.println("Hash: " + e.hash());
});
```

## Gestion des erreurs

```java
try {
    client.log(event);
} catch (AuditTrailConnectionException e) {
    // Erreur de connexion (reseau, timeout)
    log.error("Connection failed: {}", e.getMessage());
    // Retry ou fallback
} catch (AuditTrailApiException e) {
    // Erreur API (4xx, 5xx)
    log.error("API error {}: {}", e.getStatusCode(), e.getMessage());
    if (e.getStatusCode() == 429) {
        // Rate limited - attendre et reessayer
    }
} catch (AuditTrailValidationException e) {
    // Erreur de validation
    log.error("Validation failed: {}", e.getViolations());
    // Corriger les donnees
}
```

## Integration Spring Boot

### Configuration Bean

```java
@Configuration
public class AuditTrailConfig {

    @Bean
    public AuditTrailClient auditTrailClient(
            @Value("${audit-trail.server-url}") String serverUrl,
            @Value("${audit-trail.api-key}") String apiKey,
            @Value("${audit-trail.timeout:30}") int timeout) {
        return AuditTrailClient.builder()
            .serverUrl(serverUrl)
            .apiKey(apiKey)
            .timeout(Duration.ofSeconds(timeout))
            .retryAttempts(3)
            .build();
    }
}
```

### application.yml

```yaml
audit-trail:
  server-url: https://audit.example.com
  api-key: ${AUDIT_TRAIL_API_KEY}
  timeout: 30
```

### Utilisation dans un Service

```java
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AuditTrailClient auditTrail;

    @Transactional
    public Document create(CreateDocumentRequest request, User user) {
        Document doc = documentRepository.save(new Document(request));

        auditTrail.logAsync(Event.builder()
            .actor(Actor.user(user.getId(), user.getName()))
            .action(Action.create("Created document"))
            .resource(Resource.document(doc.getId(), doc.getName()))
            .metadata(EventMetadata.builder()
                .source("document-service")
                .tenantId(user.getTenantId())
                .correlationId(MDC.get("correlationId"))
                .build())
            .build());

        return doc;
    }

    @Transactional
    public Document update(String id, UpdateDocumentRequest request, User user) {
        Document doc = documentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Document not found"));

        Map<String, Object> before = doc.toMap();
        doc.update(request);
        Document updated = documentRepository.save(doc);

        auditTrail.logAsync(Event.builder()
            .actor(Actor.user(user.getId(), user.getName()))
            .action(Action.update("Updated document"))
            .resource(Resource.builder()
                .id(doc.getId())
                .type(ResourceType.DOCUMENT)
                .name(doc.getName())
                .before(before)
                .after(updated.toMap())
                .build())
            .metadata(EventMetadata.builder()
                .source("document-service")
                .tenantId(user.getTenantId())
                .build())
            .build());

        return updated;
    }
}
```

### AOP pour logging automatique

```java
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditTrailClient auditTrail;

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Object result = joinPoint.proceed();

        auditTrail.logAsync(Event.builder()
            .actor(getCurrentActor())
            .action(Action.of(audited.action(), audited.description(), audited.category()))
            .resource(extractResource(joinPoint, result))
            .metadata(EventMetadata.builder()
                .source(audited.source())
                .tenantId(getCurrentTenantId())
                .build())
            .build());

        return result;
    }
}
```

## Bonnes pratiques

1. **Utilisez l'async** pour ne pas bloquer les requetes
2. **Gerez les erreurs** avec des fallbacks appropries
3. **Incluez le correlationId** pour le tracing
4. **Loggez les changements** (before/after) pour les updates
5. **Utilisez les factory methods** pour la lisibilite
