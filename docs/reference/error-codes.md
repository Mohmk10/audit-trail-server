# Error Codes Reference

Reference complete des codes d'erreur de l'API Audit Trail.

## Format des erreurs

Toutes les erreurs suivent le format :

```json
{
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/events",
  "requestId": "req-abc123",
  "details": { ... }
}
```

## Codes HTTP

### 2xx - Succes

| Code | Description |
|------|-------------|
| 200 | OK - Requete reussie |
| 201 | Created - Ressource creee |
| 202 | Accepted - Requete acceptee (traitement asynchrone) |
| 204 | No Content - Succes sans contenu |

### 4xx - Erreurs Client

| Code | Description |
|------|-------------|
| 400 | Bad Request - Requete invalide |
| 401 | Unauthorized - Authentification requise |
| 403 | Forbidden - Acces refuse |
| 404 | Not Found - Ressource non trouvee |
| 405 | Method Not Allowed - Methode HTTP non supportee |
| 409 | Conflict - Conflit (ex: ressource deja existante) |
| 422 | Unprocessable Entity - Entite non traitable |
| 429 | Too Many Requests - Rate limit atteint |

### 5xx - Erreurs Serveur

| Code | Description |
|------|-------------|
| 500 | Internal Server Error - Erreur interne |
| 502 | Bad Gateway - Erreur de gateway |
| 503 | Service Unavailable - Service indisponible |
| 504 | Gateway Timeout - Timeout de gateway |

## Codes d'erreur applicatifs

### Authentification (AUTH_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `AUTH_MISSING_API_KEY` | 401 | API key is required | Header X-API-Key manquant |
| `AUTH_INVALID_API_KEY` | 401 | Invalid API key | Cle API invalide |
| `AUTH_EXPIRED_API_KEY` | 401 | API key has expired | Cle API expiree |
| `AUTH_DISABLED_API_KEY` | 403 | API key is disabled | Cle API desactivee |
| `AUTH_INSUFFICIENT_PERMISSIONS` | 403 | Insufficient permissions | Permissions insuffisantes |

**Exemple:**

```json
{
  "status": 401,
  "error": "Unauthorized",
  "code": "AUTH_INVALID_API_KEY",
  "message": "Invalid API key",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/events"
}
```

### Validation (VALIDATION_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `VALIDATION_ERROR` | 400 | Validation failed | Erreur de validation generale |
| `VALIDATION_REQUIRED_FIELD` | 400 | Field is required | Champ obligatoire manquant |
| `VALIDATION_INVALID_FORMAT` | 400 | Invalid format | Format invalide |
| `VALIDATION_INVALID_VALUE` | 400 | Invalid value | Valeur invalide |
| `VALIDATION_TOO_LONG` | 400 | Value too long | Valeur trop longue |
| `VALIDATION_TOO_SHORT` | 400 | Value too short | Valeur trop courte |
| `VALIDATION_BATCH_TOO_LARGE` | 400 | Batch size exceeds limit | Batch trop grand (>1000) |

**Exemple:**

```json
{
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/events",
  "details": {
    "violations": [
      {
        "field": "actor.id",
        "message": "must not be blank",
        "rejectedValue": null
      },
      {
        "field": "metadata.tenantId",
        "message": "must not be blank",
        "rejectedValue": ""
      }
    ]
  }
}
```

### Ressources (RESOURCE_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `RESOURCE_NOT_FOUND` | 404 | Resource not found | Ressource non trouvee |
| `RESOURCE_ALREADY_EXISTS` | 409 | Resource already exists | Ressource deja existante |
| `RESOURCE_CONFLICT` | 409 | Resource conflict | Conflit de ressource |
| `RESOURCE_DELETED` | 410 | Resource has been deleted | Ressource supprimee |

**Exemple:**

```json
{
  "status": 404,
  "error": "Not Found",
  "code": "RESOURCE_NOT_FOUND",
  "message": "Event not found: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/events/550e8400-e29b-41d4-a716-446655440000",
  "details": {
    "resourceType": "Event",
    "resourceId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### Tenant (TENANT_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `TENANT_NOT_FOUND` | 404 | Tenant not found | Tenant non trouve |
| `TENANT_DISABLED` | 403 | Tenant is disabled | Tenant desactive |
| `TENANT_QUOTA_EXCEEDED` | 429 | Tenant quota exceeded | Quota tenant depasse |
| `TENANT_RATE_LIMIT` | 429 | Tenant rate limit exceeded | Rate limit tenant depasse |

**Exemple:**

```json
{
  "status": 404,
  "error": "Not Found",
  "code": "TENANT_NOT_FOUND",
  "message": "Tenant not found: unknown-tenant",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/events",
  "details": {
    "tenantId": "unknown-tenant"
  }
}
```

### Rate Limiting (RATE_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `RATE_LIMIT_EXCEEDED` | 429 | Rate limit exceeded | Rate limit global depasse |
| `RATE_LIMIT_API_KEY` | 429 | API key rate limit exceeded | Rate limit API key depasse |
| `RATE_LIMIT_TENANT` | 429 | Tenant rate limit exceeded | Rate limit tenant depasse |
| `RATE_LIMIT_IP` | 429 | IP rate limit exceeded | Rate limit IP depasse |

**Exemple:**

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Rate limit exceeded. Retry after 60 seconds",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/events",
  "details": {
    "limit": 1000,
    "remaining": 0,
    "resetAt": "2025-01-15T10:31:00Z",
    "retryAfter": 60
  }
}
```

### Recherche (SEARCH_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `SEARCH_INVALID_QUERY` | 400 | Invalid search query | Requete de recherche invalide |
| `SEARCH_TIMEOUT` | 504 | Search timeout | Timeout de recherche |
| `SEARCH_INDEX_ERROR` | 500 | Search index error | Erreur d'index Elasticsearch |

**Exemple:**

```json
{
  "status": 400,
  "error": "Bad Request",
  "code": "SEARCH_INVALID_QUERY",
  "message": "Invalid search query syntax",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/search",
  "details": {
    "query": "invalid [syntax",
    "reason": "Unclosed bracket"
  }
}
```

### Rapports (REPORT_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `REPORT_NOT_READY` | 202 | Report is still generating | Rapport en cours de generation |
| `REPORT_EXPIRED` | 410 | Report has expired | Rapport expire |
| `REPORT_GENERATION_FAILED` | 500 | Report generation failed | Echec de generation |
| `REPORT_TOO_LARGE` | 413 | Report too large | Rapport trop volumineux |

**Exemple:**

```json
{
  "status": 202,
  "error": "Accepted",
  "code": "REPORT_NOT_READY",
  "message": "Report is still generating",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/reports/abc123/download",
  "details": {
    "reportId": "abc123",
    "status": "GENERATING",
    "progress": 45,
    "estimatedCompletion": "2025-01-15T10:32:00Z"
  }
}
```

### Regles (RULE_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `RULE_INVALID_CONDITION` | 400 | Invalid rule condition | Condition de regle invalide |
| `RULE_CIRCULAR_DEPENDENCY` | 400 | Circular rule dependency | Dependance circulaire |
| `RULE_LIMIT_EXCEEDED` | 400 | Rule limit exceeded | Limite de regles atteinte |

### Webhooks (WEBHOOK_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `WEBHOOK_INVALID_URL` | 400 | Invalid webhook URL | URL de webhook invalide |
| `WEBHOOK_UNREACHABLE` | 400 | Webhook endpoint unreachable | Endpoint non accessible |
| `WEBHOOK_DISABLED` | 400 | Webhook has been disabled | Webhook desactive |

### Systeme (SYSTEM_*)

| Code | HTTP | Message | Description |
|------|------|---------|-------------|
| `SYSTEM_UNAVAILABLE` | 503 | System temporarily unavailable | Systeme temporairement indisponible |
| `SYSTEM_MAINTENANCE` | 503 | System under maintenance | Maintenance en cours |
| `SYSTEM_DATABASE_ERROR` | 500 | Database error | Erreur de base de donnees |
| `SYSTEM_ELASTICSEARCH_ERROR` | 500 | Search engine error | Erreur Elasticsearch |
| `SYSTEM_KAFKA_ERROR` | 500 | Message broker error | Erreur Kafka |

## Gestion des erreurs

### SDK Java

```java
try {
    client.log(event);
} catch (AuditTrailApiException e) {
    switch (e.getErrorCode()) {
        case "AUTH_INVALID_API_KEY":
            // Verifier la cle API
            break;
        case "VALIDATION_ERROR":
            // Corriger les donnees
            e.getViolations().forEach(v ->
                log.error("{}: {}", v.getField(), v.getMessage())
            );
            break;
        case "RATE_LIMIT_EXCEEDED":
            // Attendre et reessayer
            Thread.sleep(e.getRetryAfter() * 1000);
            break;
        default:
            log.error("Unexpected error: {}", e.getMessage());
    }
}
```

### SDK JavaScript

```javascript
try {
  await client.log(event);
} catch (error) {
  if (error instanceof AuditTrailApiError) {
    switch (error.code) {
      case 'AUTH_INVALID_API_KEY':
        // Verifier la cle API
        break;
      case 'VALIDATION_ERROR':
        // Corriger les donnees
        error.violations.forEach(v =>
          console.error(`${v.field}: ${v.message}`)
        );
        break;
      case 'RATE_LIMIT_EXCEEDED':
        // Attendre et reessayer
        await sleep(error.retryAfter * 1000);
        break;
      default:
        console.error('Unexpected error:', error.message);
    }
  }
}
```

### SDK Python

```python
try:
    client.log(event)
except AuditTrailApiError as e:
    if e.code == 'AUTH_INVALID_API_KEY':
        # Verifier la cle API
        pass
    elif e.code == 'VALIDATION_ERROR':
        # Corriger les donnees
        for v in e.violations:
            print(f"{v['field']}: {v['message']}")
    elif e.code == 'RATE_LIMIT_EXCEEDED':
        # Attendre et reessayer
        time.sleep(e.retry_after)
    else:
        print(f"Unexpected error: {e.message}")
```
