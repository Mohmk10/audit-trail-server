# Events API

API pour l'ingestion et la recuperation d'evenements.

## Ingerer un evenement

```http
POST /api/v1/events
```

### Request Body

```json
{
  "actor": {
    "id": "user-123",
    "type": "USER",
    "name": "John Doe",
    "ip": "192.168.1.1",
    "userAgent": "Mozilla/5.0...",
    "attributes": {
      "department": "Engineering",
      "role": "Developer"
    }
  },
  "action": {
    "type": "CREATE",
    "description": "Created document",
    "category": "DOCUMENT"
  },
  "resource": {
    "id": "doc-456",
    "type": "DOCUMENT",
    "name": "Q4 Report",
    "before": null,
    "after": {
      "status": "draft",
      "version": 1
    }
  },
  "metadata": {
    "source": "web-app",
    "tenantId": "tenant-001",
    "correlationId": "req-789",
    "sessionId": "sess-abc",
    "tags": {
      "priority": "high",
      "environment": "production"
    }
  }
}
```

### Response (201 Created)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-01-15T10:30:00Z",
  "hash": "a1b2c3d4e5f6789...",
  "status": "STORED"
}
```

## Schemas

### Actor

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | string | Oui | Identifiant unique de l'acteur |
| `type` | enum | Oui | `USER`, `SYSTEM`, `SERVICE` |
| `name` | string | Non | Nom affichable |
| `ip` | string | Non | Adresse IP |
| `userAgent` | string | Non | User-Agent du client |
| `attributes` | object | Non | Attributs additionnels |

### Action

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `type` | string | Oui | Type d'action (libre) |
| `description` | string | Non | Description de l'action |
| `category` | string | Non | Categorie de l'action |

**Types d'action suggerees** :
- `CREATE` - Creation
- `READ` - Lecture
- `UPDATE` - Modification
- `DELETE` - Suppression
- `LOGIN` - Connexion
- `LOGOUT` - Deconnexion
- `APPROVE` - Approbation
- `REJECT` - Rejet
- `EXPORT` - Export
- `IMPORT` - Import

### Resource

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | string | Oui | Identifiant de la ressource |
| `type` | string | Oui | Type de ressource |
| `name` | string | Non | Nom affichable |
| `before` | object | Non | Etat avant modification |
| `after` | object | Non | Etat apres modification |

### Metadata

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `source` | string | Oui | Source de l'evenement |
| `tenantId` | string | Oui | Identifiant du tenant |
| `correlationId` | string | Non | ID de correlation |
| `sessionId` | string | Non | ID de session |
| `tags` | object | Non | Tags additionnels |

---

## Ingerer en batch

```http
POST /api/v1/events/batch
```

### Request Body

```json
{
  "events": [
    { ... },
    { ... },
    { ... }
  ]
}
```

**Limite** : 1000 evenements par batch

### Response (201 Created)

```json
{
  "total": 100,
  "succeeded": 98,
  "failed": 2,
  "events": [
    {
      "id": "...",
      "timestamp": "...",
      "hash": "...",
      "status": "STORED"
    }
  ],
  "errors": [
    {
      "index": 5,
      "message": "Validation failed",
      "violations": ["actor.id: must not be blank"]
    },
    {
      "index": 42,
      "message": "Validation failed",
      "violations": ["metadata.tenantId: must not be blank"]
    }
  ]
}
```

---

## Recuperer un evenement

```http
GET /api/v1/events/{id}
```

### Path Parameters

| Parametre | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Identifiant de l'evenement |

### Response (200 OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-01-15T10:30:00Z",
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
  },
  "previousHash": "xyz123...",
  "hash": "abc456...",
  "signature": "MEUCIQDk..."
}
```

### Response (404 Not Found)

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Event not found: 550e8400-..."
}
```

---

## Erreurs courantes

### Validation (400)

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "violations": [
    "actor.id: must not be blank",
    "metadata.tenantId: must not be blank"
  ]
}
```

### Tenant non trouve (404)

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Tenant not found: unknown-tenant"
}
```

---

## Exemples

### Login utilisateur

```json
{
  "actor": {
    "id": "user-123",
    "type": "USER",
    "name": "John Doe",
    "ip": "192.168.1.100"
  },
  "action": {
    "type": "LOGIN",
    "description": "User logged in successfully",
    "category": "AUTHENTICATION"
  },
  "resource": {
    "id": "session-456",
    "type": "SESSION",
    "name": "Web Session"
  },
  "metadata": {
    "source": "auth-service",
    "tenantId": "tenant-001",
    "tags": {
      "method": "password",
      "mfa": "true"
    }
  }
}
```

### Modification de document

```json
{
  "actor": {
    "id": "user-123",
    "type": "USER",
    "name": "John Doe"
  },
  "action": {
    "type": "UPDATE",
    "description": "Updated document status",
    "category": "DOCUMENT"
  },
  "resource": {
    "id": "doc-456",
    "type": "DOCUMENT",
    "name": "Q4 Report",
    "before": {
      "status": "draft",
      "version": 1
    },
    "after": {
      "status": "published",
      "version": 2
    }
  },
  "metadata": {
    "source": "document-service",
    "tenantId": "tenant-001",
    "correlationId": "req-789"
  }
}
```

### Action systeme

```json
{
  "actor": {
    "id": "batch-processor",
    "type": "SYSTEM",
    "name": "Nightly Batch Job"
  },
  "action": {
    "type": "PROCESS",
    "description": "Processed 1500 transactions",
    "category": "BATCH"
  },
  "resource": {
    "id": "batch-20250115",
    "type": "BATCH_JOB",
    "name": "Transaction Processing"
  },
  "metadata": {
    "source": "batch-service",
    "tenantId": "tenant-001",
    "tags": {
      "records_processed": "1500",
      "duration_ms": "45000"
    }
  }
}
```
