# API Overview

L'API REST d'Audit Trail utilise JSON et suit les conventions REST standard.

## Base URL

```
https://audit.example.com/api/v1
```

## Authentication

Toutes les requetes API necessitent une cle API via le header `X-API-Key` :

```bash
curl -H "X-API-Key: your-api-key" https://audit.example.com/api/v1/events
```

Voir [Authentication](authentication.md) pour plus de details.

## Content Type

Toutes les requetes doivent utiliser `application/json` :

```bash
curl -H "Content-Type: application/json" ...
```

## Endpoints

| Categorie | Base Path | Description |
|-----------|-----------|-------------|
| [Events](events.md) | `/events` | Ingestion et recuperation d'evenements |
| [Search](search.md) | `/search` | Recherche et agregations |
| [Reports](reports.md) | `/reports` | Generation de rapports |
| [Rules](rules.md) | `/rules` | Regles de detection |
| [Alerts](alerts.md) | `/alerts` | Gestion des alertes |
| [Webhooks](webhooks.md) | `/webhooks` | Configuration webhooks |
| [Admin](admin.md) | `/admin/*` | Operations administratives |

## Reponses

### Succes

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-01-15T10:30:00Z",
  ...
}
```

### Erreur

```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/events",
  "violations": [
    "actor.id: must not be blank"
  ]
}
```

## Codes HTTP

| Code | Description |
|------|-------------|
| `200` | OK - Requete reussie |
| `201` | Created - Ressource creee |
| `204` | No Content - Suppression reussie |
| `400` | Bad Request - Requete invalide |
| `401` | Unauthorized - API key manquante ou invalide |
| `403` | Forbidden - Acces refuse |
| `404` | Not Found - Ressource non trouvee |
| `409` | Conflict - Conflit (ex: doublon) |
| `429` | Too Many Requests - Rate limit depasse |
| `500` | Internal Server Error - Erreur serveur |

## Pagination

Les endpoints qui retournent des listes utilisent la pagination :

```bash
GET /api/v1/search?page=0&size=20
```

Reponse paginee :

```json
{
  "items": [...],
  "totalCount": 150,
  "page": 0,
  "size": 20,
  "totalPages": 8
}
```

Parametres :
- `page` : Numero de page (0-indexed, defaut: 0)
- `size` : Taille de page (defaut: 20, max: 100)

## Rate Limiting

- **Defaut** : 1000 requetes/minute par API key
- **Configurable** par tenant/source

Headers de reponse :

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1642248600
```

## Timestamps

Tous les timestamps utilisent le format ISO 8601 en UTC :

```
2025-01-15T10:30:00Z
```

## UUIDs

Tous les identifiants sont des UUID v4 :

```
550e8400-e29b-41d4-a716-446655440000
```

## Versioning

L'API est versionee via le path (`/api/v1`). Les versions sont maintenues pendant au moins 12 mois apres deprecation.
