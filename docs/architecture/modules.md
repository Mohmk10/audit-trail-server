# Modules

Description detaillee de chaque module du systeme.

## audit-trail-core

Le coeur du systeme contenant le domaine metier.

### Responsabilites

- Definition des entites et value objects
- Logique metier fondamentale
- Interfaces (ports) pour les adaptateurs
- Evenements du domaine

### Structure

```
audit-trail-core/
├── src/main/java/com/mohmk10/audittrail/core/
│   ├── domain/
│   │   ├── event/
│   │   │   ├── Event.java           # Aggregate root
│   │   │   ├── Actor.java           # Value object
│   │   │   ├── Action.java          # Value object
│   │   │   ├── Resource.java        # Value object
│   │   │   └── EventMetadata.java   # Value object
│   │   └── common/
│   │       └── TenantAware.java     # Interface multi-tenant
│   ├── port/
│   │   ├── in/
│   │   │   └── EventUseCase.java    # Port entrant
│   │   └── out/
│   │       └── EventRepository.java # Port sortant
│   └── service/
│       ├── HashService.java         # Service de hashing
│       └── ValidationService.java   # Service de validation
└── src/test/java/
    └── ...                          # Tests unitaires
```

### Classes principales

#### Event (Aggregate Root)

```java
@Entity
public class Event implements TenantAware {
    private UUID id;
    private Instant timestamp;
    private Actor actor;
    private Action action;
    private Resource resource;
    private EventMetadata metadata;
    private String previousHash;
    private String hash;
    private String signature;
    private String tenantId;
}
```

#### Actor (Value Object)

```java
@Embeddable
public record Actor(
    String id,
    ActorType type,
    String name,
    String ip,
    String userAgent,
    Map<String, String> attributes
) {}
```

---

## audit-trail-storage

Module de persistance avec PostgreSQL.

### Responsabilites

- Persistance des evenements
- Gestion des transactions
- Migrations de schema
- Optimisation des requetes

### Structure

```
audit-trail-storage/
├── src/main/java/com/mohmk10/audittrail/storage/
│   ├── adapter/out/
│   │   └── persistence/
│   │       ├── EventJpaRepository.java
│   │       ├── EventPersistenceAdapter.java
│   │       └── EventEntity.java
│   ├── config/
│   │   └── JpaConfig.java
│   └── service/
│       └── EventStorageService.java
└── src/main/resources/
    └── db/migration/
        ├── V1__create_events_table.sql
        ├── V2__create_indexes.sql
        └── V3__add_partitioning.sql
```

### Schema PostgreSQL

```sql
CREATE TABLE events (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    actor_id VARCHAR(255) NOT NULL,
    actor_type VARCHAR(50) NOT NULL,
    actor_name VARCHAR(255),
    action_type VARCHAR(100) NOT NULL,
    action_description TEXT,
    resource_id VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_name VARCHAR(255),
    source VARCHAR(100) NOT NULL,
    previous_hash VARCHAR(64),
    hash VARCHAR(64) NOT NULL,
    signature TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index pour les requetes frequentes
CREATE INDEX idx_events_tenant_timestamp ON events (tenant_id, timestamp DESC);
CREATE INDEX idx_events_actor ON events (tenant_id, actor_id);
CREATE INDEX idx_events_resource ON events (tenant_id, resource_id);
```

---

## audit-trail-search

Module de recherche avec Elasticsearch.

### Responsabilites

- Indexation des evenements
- Recherche full-text
- Agregations et analytics
- Suggestions et autocompletion

### Structure

```
audit-trail-search/
├── src/main/java/com/mohmk10/audittrail/search/
│   ├── adapter/
│   │   ├── in/rest/
│   │   │   └── SearchController.java
│   │   └── out/elasticsearch/
│   │       ├── EventSearchAdapter.java
│   │       └── EventDocument.java
│   ├── config/
│   │   └── ElasticsearchConfig.java
│   └── service/
│       ├── SearchService.java
│       └── IndexService.java
```

### Mapping Elasticsearch

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "timestamp": { "type": "date" },
      "tenantId": { "type": "keyword" },
      "actor": {
        "properties": {
          "id": { "type": "keyword" },
          "type": { "type": "keyword" },
          "name": { "type": "text", "fields": { "keyword": { "type": "keyword" } } }
        }
      },
      "action": {
        "properties": {
          "type": { "type": "keyword" },
          "description": { "type": "text" }
        }
      },
      "resource": {
        "properties": {
          "id": { "type": "keyword" },
          "type": { "type": "keyword" },
          "name": { "type": "text", "fields": { "keyword": { "type": "keyword" } } }
        }
      },
      "metadata": {
        "properties": {
          "source": { "type": "keyword" },
          "correlationId": { "type": "keyword" }
        }
      }
    }
  }
}
```

---

## audit-trail-reporting

Module de generation de rapports.

### Responsabilites

- Generation de rapports PDF, CSV, Excel, JSON
- Planification de rapports
- Templates personnalisables
- Export asynchrone

### Structure

```
audit-trail-reporting/
├── src/main/java/com/mohmk10/audittrail/reporting/
│   ├── adapter/
│   │   ├── in/rest/
│   │   │   └── ReportController.java
│   │   └── out/
│   │       └── storage/
│   │           └── ReportStorageAdapter.java
│   ├── domain/
│   │   ├── Report.java
│   │   ├── ReportTemplate.java
│   │   └── ReportStatus.java
│   ├── generator/
│   │   ├── ReportGenerator.java
│   │   ├── PdfReportGenerator.java
│   │   ├── CsvReportGenerator.java
│   │   ├── ExcelReportGenerator.java
│   │   └── JsonReportGenerator.java
│   └── service/
│       └── ReportService.java
```

### Formats supportes

| Format | Extension | Usage |
|--------|-----------|-------|
| PDF | .pdf | Rapports formels, impression |
| CSV | .csv | Import dans d'autres systemes |
| Excel | .xlsx | Analyse dans spreadsheet |
| JSON | .json | Integration API |

---

## audit-trail-detection

Module de detection d'anomalies et alertes.

### Responsabilites

- Moteur de regles
- Detection en temps reel
- Generation d'alertes
- Notifications (email, webhook, Slack)

### Structure

```
audit-trail-detection/
├── src/main/java/com/mohmk10/audittrail/detection/
│   ├── adapter/
│   │   ├── in/rest/
│   │   │   ├── RuleController.java
│   │   │   └── AlertController.java
│   │   └── out/
│   │       └── notification/
│   │           ├── EmailNotificationAdapter.java
│   │           ├── WebhookNotificationAdapter.java
│   │           └── SlackNotificationAdapter.java
│   ├── domain/
│   │   ├── Rule.java
│   │   ├── Alert.java
│   │   └── Notification.java
│   ├── engine/
│   │   ├── RuleEngine.java
│   │   └── RuleEvaluator.java
│   └── service/
│       ├── AlertService.java
│       └── NotificationService.java
```

### Types de regles

```java
public enum RuleType {
    THRESHOLD,      // Seuil depasse
    PATTERN,        // Pattern detecte
    ANOMALY,        // Comportement anormal
    SEQUENCE,       // Sequence d'evenements
    TIME_BASED      // Base sur l'heure
}
```

---

## audit-trail-admin

Module d'administration.

### Responsabilites

- Gestion des tenants
- Gestion des sources
- Gestion des API keys
- Gestion des utilisateurs et RBAC
- Logs d'administration

### Structure

```
audit-trail-admin/
├── src/main/java/com/mohmk10/audittrail/admin/
│   ├── adapter/in/rest/
│   │   ├── TenantController.java
│   │   ├── SourceController.java
│   │   ├── ApiKeyController.java
│   │   └── UserController.java
│   ├── domain/
│   │   ├── Tenant.java
│   │   ├── Source.java
│   │   ├── ApiKey.java
│   │   ├── User.java
│   │   └── Role.java
│   └── service/
│       ├── TenantService.java
│       ├── SourceService.java
│       ├── ApiKeyService.java
│       └── UserService.java
```

### RBAC

| Role | Permissions |
|------|-------------|
| ADMIN | Toutes les operations |
| MANAGER | CRUD tenants, sources, users |
| OPERATOR | Lecture + gestion alertes |
| VIEWER | Lecture seule |

---

## audit-trail-integration

Module d'integration externe.

### Responsabilites

- Webhooks entrants/sortants
- Integration Kafka
- Integration SIEM
- Connecteurs externes

### Structure

```
audit-trail-integration/
├── src/main/java/com/mohmk10/audittrail/integration/
│   ├── webhook/
│   │   ├── domain/
│   │   │   └── WebhookSubscription.java
│   │   ├── adapter/
│   │   │   ├── in/rest/
│   │   │   │   └── WebhookController.java
│   │   │   └── out/
│   │   │       └── WebhookDeliveryAdapter.java
│   │   └── service/
│   │       └── WebhookService.java
│   ├── kafka/
│   │   ├── consumer/
│   │   │   └── EventKafkaConsumer.java
│   │   ├── producer/
│   │   │   └── EventKafkaProducer.java
│   │   └── config/
│   │       └── KafkaConfig.java
│   └── siem/
│       ├── SiemExporter.java
│       └── SplunkAdapter.java
```

---

## audit-trail-app

Application Spring Boot principale.

### Responsabilites

- Point d'entree de l'application
- Configuration globale
- Composition des modules
- Health checks

### Structure

```
audit-trail-app/
├── src/main/java/com/mohmk10/audittrail/
│   ├── AuditTrailApplication.java
│   └── config/
│       ├── SecurityConfig.java
│       ├── WebConfig.java
│       └── ObservabilityConfig.java
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    ├── application-prod.yml
    └── logback-spring.xml
```

### Configuration

```yaml
spring:
  application:
    name: audit-trail-server
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

audit-trail:
  hash:
    algorithm: SHA-256
  retention:
    days: 365
  batch:
    max-size: 1000
```
