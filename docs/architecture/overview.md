# Architecture Overview

Vue d'ensemble de l'architecture d'Audit Trail.

## Vision

Audit Trail est concu pour etre une solution d'audit universelle, scalable et secure. L'architecture suit les principes de l'Hexagonal Architecture (Ports & Adapters) pour maximiser la testabilite et la flexibilite.

## Principes architecturaux

### 1. Hexagonal Architecture

```
                     ┌─────────────────────────────────────────┐
                     │              Application                 │
                     │  ┌─────────────────────────────────────┐ │
   ┌─────────┐       │  │            Domain                   │ │       ┌─────────┐
   │  REST   │◄──────┼──┤  - Entities                         ├─┼──────►│ Postgres│
   │  API    │       │  │  - Value Objects                    │ │       └─────────┘
   └─────────┘       │  │  - Domain Services                  │ │
                     │  │  - Domain Events                    │ │       ┌─────────┐
   ┌─────────┐       │  └─────────────────────────────────────┘ │       │Elastic- │
   │  Kafka  │◄──────┼──┤                                       ├──────►│search   │
   │Consumer │       │  │            Ports                      │       └─────────┘
   └─────────┘       │  │  - In: Use Cases                     │ │
                     │  │  - Out: Repositories                 │ │       ┌─────────┐
   ┌─────────┐       │  │                                       │       │  Redis  │
   │ Webhook │◄──────┼──┤            Adapters                  ├──────►└─────────┘
   │Listener │       │  │  - In: Controllers, Listeners        │ │
   └─────────┘       │  │  - Out: JPA, ES, Redis, HTTP         │ │       ┌─────────┐
                     │  └─────────────────────────────────────┘ │       │  Kafka  │
                     └─────────────────────────────────────────┘       └─────────┘
```

### 2. Domain-Driven Design (DDD)

- **Bounded Contexts** : Chaque module represente un contexte metier distinct
- **Aggregates** : Event, Alert, Report sont les aggregates principaux
- **Value Objects** : Actor, Action, Resource sont immutables
- **Domain Events** : Communication asynchrone entre modules

### 3. CQRS (Command Query Responsibility Segregation)

- **Commands** : Ingestion d'evenements via PostgreSQL
- **Queries** : Recherche via Elasticsearch
- **Synchronisation** : Via Redis et events internes

## Modules

```
audit-trail-server/
├── audit-trail-core/          # Domaine et logique metier
├── audit-trail-storage/       # Persistence (PostgreSQL)
├── audit-trail-search/        # Recherche (Elasticsearch)
├── audit-trail-reporting/     # Generation de rapports
├── audit-trail-detection/     # Detection et alertes
├── audit-trail-admin/         # Administration
├── audit-trail-integration/   # Integrations externes
└── audit-trail-app/           # Application Spring Boot
```

### Dependances entre modules

```
                    ┌─────────────────┐
                    │ audit-trail-app │
                    └────────┬────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ audit-trail-    │ │ audit-trail-    │ │ audit-trail-    │
│ integration     │ │ detection       │ │ reporting       │
└────────┬────────┘ └────────┬────────┘ └────────┬────────┘
         │                   │                   │
         │          ┌────────┴────────┐          │
         │          │                 │          │
         ▼          ▼                 ▼          ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ audit-trail-    │ │ audit-trail-    │ │ audit-trail-    │
│ admin           │ │ search          │ │ storage         │
└────────┬────────┘ └────────┬────────┘ └────────┬────────┘
         │                   │                   │
         └───────────────────┼───────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ audit-trail-core│
                    └─────────────────┘
```

## Flux de donnees

### Ingestion d'evenement

```
1. Client SDK           2. API Gateway          3. Event Service        4. Storage
   ┌───────┐               ┌───────┐               ┌───────┐             ┌───────┐
   │       │──POST /api───►│       │──validate────►│       │──persist───►│Postgres│
   │  SDK  │               │ REST  │               │Service│             └───────┘
   │       │◄─response────│  API  │◄─response─────│       │
   └───────┘               └───────┘               └───┬───┘
                                                       │
                           5. Indexation               │ publish event
                              ┌───────┐                ▼
                              │Elastic│◄──index───┌───────┐
                              │search │           │ Event │
                              └───────┘           │ Bus   │
                                                  └───┬───┘
                           6. Detection               │
                              ┌───────┐               │
                              │Rules  │◄──evaluate────┘
                              │Engine │
                              └───────┘
```

### Recherche

```
1. Client               2. Search API           3. Elasticsearch
   ┌───────┐               ┌───────┐               ┌───────┐
   │       │──POST /search─►│       │──query──────►│       │
   │Client │               │Search │               │  ES   │
   │       │◄─results──────│  API  │◄─results─────│       │
   └───────┘               └───────┘               └───────┘
```

## Composants techniques

### API Layer

- **Spring WebFlux** : Reactive programming pour haute concurrence
- **Spring Security** : Authentification API Key
- **OpenAPI** : Documentation automatique
- **Validation** : Bean Validation (JSR-380)

### Domain Layer

- **POJOs immutables** : Entities et Value Objects
- **Domain Services** : Logique metier complexe
- **Domain Events** : Communication inter-modules

### Persistence Layer

- **PostgreSQL** : Stockage primaire (ACID)
- **Spring Data JPA** : ORM avec Hibernate
- **Flyway** : Migrations de schema

### Search Layer

- **Elasticsearch** : Full-text search et analytics
- **Spring Data Elasticsearch** : Integration
- **Index templates** : Mapping optimise

### Caching Layer

- **Redis** : Cache distribue
- **Spring Cache** : Abstraction de cache
- **TTL configurable** : Par type de donnee

### Messaging Layer

- **Kafka** : Event streaming
- **Spring Kafka** : Integration
- **Consumer groups** : Scalabilite horizontale

## Securite

### Authentication

```
┌─────────┐     ┌─────────────┐     ┌─────────────┐
│ Request │────►│ API Key     │────►│ HMAC-SHA256 │
│         │     │ Filter      │     │ Validation  │
└─────────┘     └─────────────┘     └──────┬──────┘
                                           │
                      ┌────────────────────┴────────────────────┐
                      │                                         │
                      ▼                                         ▼
              ┌─────────────┐                           ┌─────────────┐
              │   Valid     │                           │   Invalid   │
              │   Access    │                           │   401/403   │
              └─────────────┘                           └─────────────┘
```

### Multi-tenancy

- Isolation des donnees par `tenantId`
- Filtrage automatique sur toutes les requetes
- Audit des acces cross-tenant

### Integrite des donnees

- Hash SHA-256 par evenement
- Chaine de hash (blockchain-like)
- Signature numerique optionnelle

## Scalabilite

### Horizontale

```
                    ┌─────────────────┐
                    │  Load Balancer  │
                    └────────┬────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
    ┌───────────┐      ┌───────────┐      ┌───────────┐
    │  Pod 1    │      │  Pod 2    │      │  Pod 3    │
    │ Audit     │      │ Audit     │      │ Audit     │
    │ Trail     │      │ Trail     │      │ Trail     │
    └─────┬─────┘      └─────┬─────┘      └─────┬─────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
    ┌───────────┐      ┌───────────┐      ┌───────────┐
    │ PostgreSQL│      │Elasticsearch│     │   Redis   │
    │ (Primary) │      │ (Cluster)  │      │ (Cluster) │
    └───────────┘      └───────────┘      └───────────┘
```

### Metriques cibles

| Metrique | Cible | Notes |
|----------|-------|-------|
| Throughput | 10,000 events/s | Par instance |
| Latence P99 | < 100ms | Ingestion |
| Latence recherche | < 200ms | Full-text |
| Disponibilite | 99.9% | SLA |
| Retention | Illimitee | Configurable |

## Observabilite

### Logging

- Format JSON structure
- Correlation ID propagation
- Log levels configurables

### Metrics

- Micrometer + Prometheus
- JVM, HTTP, business metrics
- Dashboards Grafana

### Tracing

- OpenTelemetry
- Traces distribues
- Integration Jaeger/Zipkin
