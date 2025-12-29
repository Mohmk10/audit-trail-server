<div align="center">

# 🔍 Audit Trail Universel

**Le système nerveux de la traçabilité**

<!-- Status Badges -->
[![CI](https://github.com/Mohmk10/audit-trail-server/actions/workflows/ci.yml/badge.svg)](https://github.com/Mohmk10/audit-trail-server/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/Mohmk10/audit-trail-server/branch/main/graph/badge.svg)](https://codecov.io/gh/Mohmk10/audit-trail-server)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

<!-- Technology Badges - Backend -->
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.11-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)

<!-- Technology Badges - Testing -->
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge)
![Testcontainers](https://img.shields.io/badge/Testcontainers-328BA9?style=for-the-badge&logo=docker&logoColor=white)

<!-- Technology Badges - SDKs -->
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Python](https://img.shields.io/badge/Python-3.10+-3776AB?style=for-the-badge&logo=python&logoColor=white)
![Go](https://img.shields.io/badge/Go-1.21-00ADD8?style=for-the-badge&logo=go&logoColor=white)

<!-- Technology Badges - Infrastructure -->
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![Helm](https://img.shields.io/badge/Helm-0F1689?style=for-the-badge&logo=helm&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

<!-- Package Badges -->
[![Docker Hub](https://img.shields.io/docker/v/devmohmk/audit-trail-server?label=Docker%20Hub&style=for-the-badge&logo=docker)](https://hub.docker.com/r/devmohmk/audit-trail-server)
[![npm](https://img.shields.io/npm/v/@mohmk10/audit-trail-sdk?style=for-the-badge&logo=npm)](https://www.npmjs.com/package/@mohmk10/audit-trail-sdk)
[![PyPI](https://img.shields.io/pypi/v/audit-trail-sdk?style=for-the-badge&logo=pypi)](https://pypi.org/project/audit-trail-sdk/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.mohmk10/audit-trail-sdk?style=for-the-badge&logo=apachemaven)](https://search.maven.org/artifact/io.github.mohmk10/audit-trail-sdk)

---

[📖 Documentation](docs/) · [🔌 API Reference](docs/api/) · [🚀 Quick Start](#-quick-start) · [📦 SDKs](#-sdks)

</div>

---

## Presentation

**Audit Trail Universel** est un systeme de tracabilite enterprise-grade qui capture, stocke de maniere immuable, et permet de retrouver instantanement toute action dans n'importe quel systeme.

### Pourquoi Audit Trail ?

- **Immutabilite cryptographique** - Hash chain SHA-256 + signatures ECDSA
- **Haute performance** - 10,000+ events/sec, latence < 50ms
- **Recherche puissante** - Elasticsearch full-text avec agregations
- **Detection temps reel** - Regles personnalisables + alertes instantanees
- **Reporting complet** - PDF, CSV, Excel, JSON avec certification
- **Multi-tenant** - Isolation complete, RBAC granulaire
- **Integrations** - Webhooks, Kafka, Splunk, ELK, S3

---

## Cas d'Usage

| Secteur | Utilisation |
|---------|-------------|
| **Finance** | Tracabilite des transactions, audit SOX/PCI-DSS |
| **Sante** | Conformite HIPAA, acces aux dossiers patients |
| **Tech** | Logs applicatifs structures, debugging |
| **RH** | Historique des decisions, RGPD |
| **Securite** | Detection d'intrusion, forensics |
| **Compliance** | Preuves pour regulateurs, audits externes |

---

## Quick Start

### Option 1 : Docker Compose (recommande)

```bash
# Cloner le repo
git clone https://github.com/Mohmk10/audit-trail-server.git
cd audit-trail-server

# Demarrer la stack complete
docker-compose -f docker/docker-compose.yml up -d

# Verifier que c'est pret
curl http://localhost:8080/actuator/health
```

### Option 2 : Docker seul

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/audittrail \
  -e SPRING_ELASTICSEARCH_URIS=http://host:9200 \
  devmohmk/audit-trail-server:latest
```

### Option 3 : Kubernetes

```bash
helm repo add audit-trail https://Mohmk10.github.io/audit-trail-server
helm install my-audit-trail audit-trail/audit-trail
```

---

## Premier Evenement

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "actor": {
      "id": "user-123",
      "type": "USER",
      "name": "John Doe"
    },
    "action": {
      "type": "CREATE",
      "description": "Created document"
    },
    "resource": {
      "id": "doc-456",
      "type": "DOCUMENT",
      "name": "Q4 Report"
    },
    "metadata": {
      "source": "web-app",
      "tenantId": "tenant-001"
    }
  }'
```

Reponse :

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-01-15T10:30:00Z",
  "hash": "a1b2c3d4e5f6...",
  "status": "STORED"
}
```

---

## SDKs

Integrez Audit Trail en quelques lignes de code :

### Java

```xml
<dependency>
    <groupId>io.github.mohmk10</groupId>
    <artifactId>audit-trail-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
AuditTrailClient client = AuditTrailClient.builder()
    .serverUrl("https://audit.example.com")
    .apiKey("your-api-key")
    .build();

client.log(Event.builder()
    .actor(Actor.user("user-123", "John Doe"))
    .action(Action.create("Created document"))
    .resource(Resource.document("doc-456", "Q4 Report"))
    .build());
```

### JavaScript / TypeScript

```bash
npm install @mohmk10/audit-trail-sdk
```

```typescript
import { AuditTrailClient, ActorBuilder, ActionBuilder, ResourceBuilder } from '@mohmk10/audit-trail-sdk';

const client = AuditTrailClient.builder()
  .serverUrl('https://audit.example.com')
  .apiKey('your-api-key')
  .build();

await client.log({
  actor: ActorBuilder.user('user-123', 'John Doe'),
  action: ActionBuilder.create('Created document'),
  resource: ResourceBuilder.document('doc-456', 'Q4 Report'),
  metadata: { source: 'web-app', tenantId: 'tenant-001' }
});
```

### Python

```bash
pip install audit-trail-sdk
```

```python
from audit_trail_sdk import AuditTrailClient, Actor, Action, Resource, EventMetadata, Event

client = AuditTrailClient.builder() \
    .server_url("https://audit.example.com") \
    .api_key("your-api-key") \
    .build()

client.log(Event.create(
    actor=Actor.user("user-123", "John Doe"),
    action=Action.create("Created document"),
    resource=Resource.document("doc-456", "Q4 Report"),
    metadata=EventMetadata.create("web-app", "tenant-001")
))
```

### Go

```bash
go get github.com/Mohmk10/audit-trail-server/audit-trail-sdk-go
```

```go
client, _ := audittrail.NewClientBuilder().
    ServerURL("https://audit.example.com").
    APIKey("your-api-key").
    Build()

client.Log(ctx, audittrail.NewEvent(
    audittrail.NewUserActor("user-123", "John Doe"),
    audittrail.CreateAction("Created document"),
    audittrail.DocumentResource("doc-456", "Q4 Report"),
    audittrail.NewEventMetadata("web-app", "tenant-001"),
))
```

[Documentation complete des SDKs](docs/sdks/)

---

## Architecture

```
+------------------------------------------------------------------+
|                           CLIENTS                                 |
+----------------+----------------+----------------+----------------+
|   SDK Java     |    SDK JS      |  SDK Python    |    SDK Go      |
+----------------+----------------+----------------+----------------+
                              |
                       +------v------+
                       |   API REST  |
                       +------+------+
                              |
       +----------------------+----------------------+
       |                      |                      |
+------v------+        +------v------+       +------v------+
|  Ingestion  |        |   Search    |       |  Reporting  |
|   Service   |        |   Service   |       |   Service   |
+------+------+        +------+------+       +-------------+
       |                      |
       |               +------v------+
       |               |Elasticsearch|
       |               +-------------+
       |
+------v------+                              +-------------+
|   Storage   |                              |  Detection  |
|   Service   |                              |   Service   |
+------+------+                              +-------------+
       |
+------v------+
| PostgreSQL  |  <- Hash Chain + Signatures ECDSA
+-------------+
```

[Documentation architecture complete](docs/architecture/)

---

## API Endpoints

| Categorie | Endpoints | Description |
|-----------|-----------|-------------|
| Events | 3 | Ingestion single/batch, get by ID |
| Search | 5 | Recherche avancee, quick search, timeline, aggregations |
| Reports | 6 | Generation PDF/CSV/Excel/JSON, download |
| Rules | 7 | CRUD regles de detection |
| Alerts | 6 | Liste, acknowledge, resolve, stats |
| Webhooks | 9 | CRUD webhooks, test, deliveries |
| Admin | 20+ | Tenants, sources, API keys, users |

[Documentation API complete](docs/api/)

---

## Configuration

### Variables d'environnement principales

| Variable | Description | Defaut |
|----------|-------------|--------|
| `SPRING_DATASOURCE_URL` | URL PostgreSQL | `jdbc:postgresql://localhost:5432/audittrail` |
| `SPRING_ELASTICSEARCH_URIS` | URL Elasticsearch | `http://localhost:9200` |
| `SPRING_DATA_REDIS_HOST` | Host Redis | `localhost` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Serveurs Kafka | `localhost:9092` |
| `AUDIT_TRAIL_WEBHOOK_ENABLED` | Activer webhooks | `true` |
| `AUDIT_TRAIL_KAFKA_ENABLED` | Activer Kafka | `false` |

[Guide de configuration complet](docs/getting-started/configuration.md)

---

## Tests

```bash
# Tous les tests
mvn test

# Tests unitaires uniquement
mvn test -Dtest="!*IntegrationTest"

# Tests d'integration (Docker requis)
mvn test -Dtest="*IntegrationTest"

# Coverage
mvn test jacoco:report
```

| Module | Tests | Coverage |
|--------|-------|----------|
| Core | 111 | ~85% |
| Storage | 85 | ~80% |
| Ingestion | 156 | 84% |
| Search | 131 | 88% |
| Reporting | 122 | >80% |
| Detection | 191 | >80% |
| Admin | 409 | >80% |
| Integration | 176 | >80% |
| **Total** | **1381** | **>80%** |

---

## Contribution

Les contributions sont les bienvenues ! Voir [CONTRIBUTING.md](CONTRIBUTING.md).

```bash
# Fork + clone
git clone https://github.com/YOUR_USERNAME/audit-trail-server.git

# Creer une branche
git checkout -b feature/amazing-feature

# Commit
git commit -m "feat: add amazing feature"

# Push + PR
git push origin feature/amazing-feature
```

---

## License

MIT (c) [mohmk10](https://github.com/mohmk10)

Voir [LICENSE](LICENSE) pour plus de details.

---

<div align="center">

**[Retour en haut](#audit-trail-universel)**

Made with passion by Mohamed

</div>
