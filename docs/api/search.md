# Search API

API de recherche avancee d'evenements.

## Recherche avancee

```http
POST /api/v1/search
```

### Request Body

```json
{
  "tenantId": "tenant-001",
  "actorId": "user-123",
  "actorType": "USER",
  "actionType": "CREATE",
  "resourceId": "doc-456",
  "resourceType": "DOCUMENT",
  "source": "web-app",
  "fromDate": "2025-01-01T00:00:00Z",
  "toDate": "2025-01-31T23:59:59Z",
  "query": "Q4 Report",
  "page": 0,
  "size": 20
}
```

### Parametres

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `tenantId` | string | Oui | Identifiant du tenant |
| `actorId` | string | Non | Filtrer par acteur |
| `actorType` | string | Non | Filtrer par type d'acteur |
| `actionType` | string | Non | Filtrer par type d'action |
| `resourceId` | string | Non | Filtrer par ressource |
| `resourceType` | string | Non | Filtrer par type de ressource |
| `source` | string | Non | Filtrer par source |
| `fromDate` | datetime | Non | Date de debut |
| `toDate` | datetime | Non | Date de fin |
| `query` | string | Non | Recherche full-text |
| `page` | integer | Non | Page (defaut: 0) |
| `size` | integer | Non | Taille (defaut: 20, max: 100) |

### Response (200 OK)

```json
{
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "timestamp": "2025-01-15T10:30:00Z",
      "actor": { ... },
      "action": { ... },
      "resource": { ... },
      "metadata": { ... }
    }
  ],
  "totalCount": 150,
  "page": 0,
  "size": 20,
  "totalPages": 8
}
```

---

## Recherche rapide

```http
GET /api/v1/search/quick
```

Recherche full-text simple.

### Query Parameters

| Parametre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `q` | string | Oui | Terme de recherche |
| `tenantId` | string | Oui | Identifiant du tenant |
| `page` | integer | Non | Page (defaut: 0) |
| `size` | integer | Non | Taille (defaut: 20) |

### Exemple

```bash
GET /api/v1/search/quick?q=login+failed&tenantId=tenant-001&size=50
```

### Response (200 OK)

```json
{
  "items": [...],
  "totalCount": 25,
  "page": 0,
  "size": 50,
  "totalPages": 1
}
```

---

## Recherche par acteur

```http
GET /api/v1/search/by-actor/{actorId}
```

Tous les evenements d'un acteur.

### Path Parameters

| Parametre | Type | Description |
|-----------|------|-------------|
| `actorId` | string | Identifiant de l'acteur |

### Query Parameters

| Parametre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `tenantId` | string | Oui | Identifiant du tenant |
| `fromDate` | datetime | Non | Date de debut |
| `toDate` | datetime | Non | Date de fin |
| `page` | integer | Non | Page |
| `size` | integer | Non | Taille |

---

## Recherche par ressource

```http
GET /api/v1/search/by-resource/{resourceId}
```

Tous les evenements sur une ressource.

### Path Parameters

| Parametre | Type | Description |
|-----------|------|-------------|
| `resourceId` | string | Identifiant de la ressource |

---

## Timeline

```http
GET /api/v1/search/timeline
```

Evenements groupes par periode.

### Query Parameters

| Parametre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `tenantId` | string | Oui | Identifiant du tenant |
| `fromDate` | datetime | Oui | Date de debut |
| `toDate` | datetime | Oui | Date de fin |
| `interval` | string | Non | `hour`, `day`, `week`, `month` (defaut: `day`) |

### Response (200 OK)

```json
{
  "tenantId": "tenant-001",
  "fromDate": "2025-01-01T00:00:00Z",
  "toDate": "2025-01-31T23:59:59Z",
  "interval": "day",
  "buckets": [
    {
      "key": "2025-01-01",
      "count": 150,
      "events": [...]
    },
    {
      "key": "2025-01-02",
      "count": 230,
      "events": [...]
    }
  ]
}
```

---

## Agregations

```http
POST /api/v1/search/aggregations
```

Statistiques agregees.

### Request Body

```json
{
  "tenantId": "tenant-001",
  "fromDate": "2025-01-01T00:00:00Z",
  "toDate": "2025-01-31T23:59:59Z",
  "groupBy": ["actionType", "actorType"]
}
```

### Response (200 OK)

```json
{
  "aggregations": {
    "actionType": {
      "CREATE": 500,
      "READ": 1200,
      "UPDATE": 300,
      "DELETE": 50
    },
    "actorType": {
      "USER": 1800,
      "SYSTEM": 200,
      "SERVICE": 50
    }
  },
  "totalCount": 2050
}
```

---

## Operateurs de recherche

### Full-text

La recherche full-text supporte :

- **Termes simples** : `login`
- **Phrases** : `"failed login"`
- **Wildcards** : `user*`
- **Booleens** : `login AND failed`

### Filtres

Les filtres utilisent la correspondance exacte :

```json
{
  "actorId": "user-123",
  "actionType": "LOGIN"
}
```

### Dates

Format ISO 8601 :

```json
{
  "fromDate": "2025-01-01T00:00:00Z",
  "toDate": "2025-01-31T23:59:59Z"
}
```

---

## Exemples

### Rechercher les connexions echouees

```json
{
  "tenantId": "tenant-001",
  "actionType": "LOGIN_FAILED",
  "fromDate": "2025-01-01T00:00:00Z",
  "toDate": "2025-01-31T23:59:59Z"
}
```

### Historique d'un utilisateur

```json
{
  "tenantId": "tenant-001",
  "actorId": "user-123",
  "size": 100
}
```

### Actions sur un document

```json
{
  "tenantId": "tenant-001",
  "resourceId": "doc-456",
  "resourceType": "DOCUMENT"
}
```

### Recherche full-text avec filtres

```json
{
  "tenantId": "tenant-001",
  "query": "password reset",
  "actionType": "UPDATE",
  "fromDate": "2025-01-01T00:00:00Z"
}
```
