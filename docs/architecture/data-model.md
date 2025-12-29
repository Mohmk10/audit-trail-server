# Data Model

Modele de donnees complet d'Audit Trail.

## Vue d'ensemble

```
┌──────────────────────────────────────────────────────────────────┐
│                           Event                                   │
│  ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌──────────────────┐   │
│  │  Actor  │  │ Action  │  │ Resource │  │ EventMetadata    │   │
│  └─────────┘  └─────────┘  └──────────┘  └──────────────────┘   │
└──────────────────────────────────────────────────────────────────┘

┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│    Tenant     │  │    Source     │  │    ApiKey     │
└───────────────┘  └───────────────┘  └───────────────┘

┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│     Rule      │  │     Alert     │  │ Notification  │
└───────────────┘  └───────────────┘  └───────────────┘

┌───────────────┐  ┌───────────────┐
│    Report     │  │   Webhook     │
└───────────────┘  └───────────────┘
```

## Entites principales

### Event

L'entite centrale du systeme.

```
Event
├── id: UUID                    # Identifiant unique
├── timestamp: Instant          # Horodatage precis
├── actor: Actor                # Qui a fait l'action
├── action: Action              # Quelle action
├── resource: Resource          # Sur quelle ressource
├── metadata: EventMetadata     # Metadonnees
├── previousHash: String        # Hash precedent (chaine)
├── hash: String                # Hash SHA-256
├── signature: String           # Signature numerique (opt.)
├── tenantId: String            # Isolation multi-tenant
└── createdAt: Instant          # Date de creation
```

#### Actor (Value Object)

```
Actor
├── id: String                  # Identifiant de l'acteur
├── type: ActorType             # USER, SYSTEM, SERVICE
├── name: String                # Nom affichable
├── ip: String                  # Adresse IP
├── userAgent: String           # User-Agent
└── attributes: Map<String,String>  # Attributs personnalises
```

**ActorType enum:**
- `USER` - Utilisateur humain
- `SYSTEM` - Processus systeme
- `SERVICE` - Service/application

#### Action (Value Object)

```
Action
├── type: String                # Type d'action (CREATE, READ, etc.)
├── description: String         # Description lisible
└── category: String            # Categorie (DOCUMENT, AUTH, etc.)
```

**Types d'action standards:**
| Type | Description |
|------|-------------|
| CREATE | Creation d'une ressource |
| READ | Lecture/consultation |
| UPDATE | Modification |
| DELETE | Suppression |
| LOGIN | Connexion |
| LOGOUT | Deconnexion |
| APPROVE | Approbation |
| REJECT | Rejet |
| EXPORT | Export de donnees |
| IMPORT | Import de donnees |

#### Resource (Value Object)

```
Resource
├── id: String                  # Identifiant de la ressource
├── type: String                # Type (DOCUMENT, USER, etc.)
├── name: String                # Nom affichable
├── before: Map<String,Object>  # Etat avant modification
└── after: Map<String,Object>   # Etat apres modification
```

**Types de ressource standards:**
| Type | Description |
|------|-------------|
| DOCUMENT | Document |
| USER | Utilisateur |
| TRANSACTION | Transaction |
| CONFIGURATION | Configuration |
| SESSION | Session |
| API_KEY | Cle API |

#### EventMetadata (Value Object)

```
EventMetadata
├── source: String              # Source de l'evenement
├── tenantId: String            # Tenant
├── correlationId: String       # ID de correlation
├── sessionId: String           # ID de session
└── tags: Map<String,String>    # Tags personnalises
```

---

### Tenant

Organisation/client dans le systeme multi-tenant.

```
Tenant
├── id: UUID
├── code: String                # Code unique (slug)
├── name: String                # Nom affichable
├── description: String
├── enabled: boolean
├── retentionDays: int          # Duree de retention
├── maxEventsPerDay: long       # Limite quotidienne
├── settings: Map<String,Object>
├── createdAt: Instant
└── updatedAt: Instant
```

### Source

Source d'evenements (application, service).

```
Source
├── id: UUID
├── tenantId: String
├── code: String                # Code unique par tenant
├── name: String
├── description: String
├── type: SourceType            # WEB, MOBILE, API, BATCH
├── enabled: boolean
├── settings: Map<String,Object>
├── createdAt: Instant
└── updatedAt: Instant
```

**SourceType enum:**
- `WEB` - Application web
- `MOBILE` - Application mobile
- `API` - Service API
- `BATCH` - Processus batch
- `IOT` - Dispositif IoT

### ApiKey

Cle d'API pour l'authentification.

```
ApiKey
├── id: UUID
├── tenantId: String
├── keyId: String               # Identifiant public
├── keyHash: String             # Hash HMAC-SHA256
├── name: String
├── description: String
├── permissions: Set<Permission>
├── rateLimit: int              # Requetes/minute
├── enabled: boolean
├── expiresAt: Instant
├── lastUsedAt: Instant
├── createdAt: Instant
└── updatedAt: Instant
```

---

### Rule

Regle de detection.

```
Rule
├── id: UUID
├── tenantId: String
├── name: String
├── description: String
├── type: RuleType              # THRESHOLD, PATTERN, etc.
├── condition: RuleCondition    # Condition a evaluer
├── actions: List<RuleAction>   # Actions declenchees
├── enabled: boolean
├── priority: int
├── createdAt: Instant
└── updatedAt: Instant
```

#### RuleCondition

```
RuleCondition
├── field: String               # Champ a evaluer
├── operator: Operator          # EQ, NE, GT, LT, CONTAINS, etc.
├── value: Object               # Valeur de comparaison
├── window: Duration            # Fenetre temporelle
└── threshold: int              # Seuil
```

**Operator enum:**
- `EQ` - Egal
- `NE` - Different
- `GT` - Superieur
- `GTE` - Superieur ou egal
- `LT` - Inferieur
- `LTE` - Inferieur ou egal
- `CONTAINS` - Contient
- `MATCHES` - Correspond (regex)
- `IN` - Dans la liste

### Alert

Alerte generee par une regle.

```
Alert
├── id: UUID
├── tenantId: String
├── ruleId: UUID
├── ruleName: String
├── severity: Severity          # LOW, MEDIUM, HIGH, CRITICAL
├── status: AlertStatus         # NEW, ACKNOWLEDGED, RESOLVED
├── title: String
├── description: String
├── eventIds: List<UUID>        # Evenements declencheurs
├── acknowledgedBy: String
├── acknowledgedAt: Instant
├── resolvedBy: String
├── resolvedAt: Instant
├── notes: String
├── createdAt: Instant
└── updatedAt: Instant
```

**Severity enum:**
- `LOW` - Information
- `MEDIUM` - Attention requise
- `HIGH` - Action requise
- `CRITICAL` - Action immediate

**AlertStatus enum:**
- `NEW` - Nouvelle alerte
- `ACKNOWLEDGED` - Prise en compte
- `RESOLVED` - Resolue
- `DISMISSED` - Ignoree

---

### Report

Rapport genere.

```
Report
├── id: UUID
├── tenantId: String
├── name: String
├── description: String
├── type: ReportType            # ACTIVITY, COMPLIANCE, AUDIT
├── format: ReportFormat        # PDF, CSV, EXCEL, JSON
├── parameters: ReportParameters
├── status: ReportStatus        # PENDING, GENERATING, COMPLETED, FAILED
├── filePath: String
├── fileSize: long
├── generatedAt: Instant
├── expiresAt: Instant
├── createdBy: String
├── createdAt: Instant
└── updatedAt: Instant
```

### Webhook

Subscription webhook.

```
Webhook
├── id: UUID
├── tenantId: String
├── name: String
├── url: String                 # URL de destination
├── secret: String              # Secret pour signature
├── events: Set<String>         # Types d'evenements
├── enabled: boolean
├── retryPolicy: RetryPolicy
├── headers: Map<String,String>
├── lastDeliveryAt: Instant
├── lastDeliveryStatus: int
├── createdAt: Instant
└── updatedAt: Instant
```

---

## Relations

```
┌─────────────────┐
│     Tenant      │
└────────┬────────┘
         │
         │ 1:N
         ▼
┌─────────────────┐         ┌─────────────────┐
│     Source      │         │     ApiKey      │
└─────────────────┘         └─────────────────┘
         │
         │ 1:N
         ▼
┌─────────────────┐
│      Event      │
└────────┬────────┘
         │
         │ N:1
         ▼
┌─────────────────┐         ┌─────────────────┐
│      Rule       │────────►│      Alert      │
└─────────────────┘  1:N    └─────────────────┘
```

## Indexes

### PostgreSQL

```sql
-- Events
CREATE INDEX idx_events_tenant_timestamp ON events (tenant_id, timestamp DESC);
CREATE INDEX idx_events_tenant_actor ON events (tenant_id, actor_id);
CREATE INDEX idx_events_tenant_resource ON events (tenant_id, resource_id);
CREATE INDEX idx_events_tenant_action ON events (tenant_id, action_type);
CREATE INDEX idx_events_tenant_source ON events (tenant_id, source);
CREATE INDEX idx_events_hash ON events (hash);

-- Alerts
CREATE INDEX idx_alerts_tenant_status ON alerts (tenant_id, status);
CREATE INDEX idx_alerts_tenant_severity ON alerts (tenant_id, severity);
CREATE INDEX idx_alerts_rule ON alerts (rule_id);
```

### Elasticsearch

```json
{
  "settings": {
    "number_of_shards": 5,
    "number_of_replicas": 1,
    "index": {
      "sort.field": ["tenantId", "timestamp"],
      "sort.order": ["asc", "desc"]
    }
  }
}
```

## Partitioning

### Strategy par temps

```sql
-- Partitioning par mois
CREATE TABLE events (
    ...
) PARTITION BY RANGE (timestamp);

CREATE TABLE events_2025_01 PARTITION OF events
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE events_2025_02 PARTITION OF events
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
```

### Strategy par tenant

```sql
-- Partitioning par tenant (pour tres gros tenants)
CREATE TABLE events (
    ...
) PARTITION BY LIST (tenant_id);

CREATE TABLE events_tenant_001 PARTITION OF events
    FOR VALUES IN ('tenant-001');
```
