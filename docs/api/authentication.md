# Authentication

Audit Trail utilise l'authentification par API Key.

## API Key

Chaque requete doit inclure une API Key valide dans le header `X-API-Key` :

```bash
curl -X GET https://audit.example.com/api/v1/events/{id} \
  -H "X-API-Key: atk_abc123def456..."
```

## Obtenir une API Key

Les API Keys sont creees via l'API Admin :

```bash
curl -X POST https://audit.example.com/api/v1/admin/api-keys \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "my-tenant",
    "sourceId": "my-source-id",
    "name": "Production API Key"
  }'
```

Reponse :

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "tenantId": "my-tenant",
  "sourceId": "source-id",
  "name": "Production API Key",
  "key": "atk_abc123def456ghi789...",
  "status": "ACTIVE",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

**Important** : La cle complete (`key`) n'est retournee qu'a la creation. Conservez-la de maniere securisee.

## Format de la cle

Les API Keys ont le format :

```
atk_<base64-encoded-random-bytes>
```

- Prefixe : `atk_` (audit trail key)
- Longueur : ~64 caracteres

## Stockage securise

La cle est hashee avec HMAC-SHA256 avant stockage. Seul le hash est conserve en base.

## Rotation de cle

Pour faire tourner une API Key :

```bash
curl -X POST https://audit.example.com/api/v1/admin/api-keys/{id}/rotate
```

Cela :
1. Genere une nouvelle cle
2. Invalide l'ancienne immediatement
3. Retourne la nouvelle cle

## Revocation

Pour revoquer une API Key :

```bash
curl -X DELETE https://audit.example.com/api/v1/admin/api-keys/{id}
```

Ou changer son statut :

```bash
curl -X PUT https://audit.example.com/api/v1/admin/api-keys/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "status": "REVOKED"
  }'
```

## Statuts

| Statut | Description |
|--------|-------------|
| `ACTIVE` | Cle utilisable |
| `INACTIVE` | Cle desactivee temporairement |
| `REVOKED` | Cle revoquee definitivement |
| `EXPIRED` | Cle expiree |

## Erreurs d'authentification

### Cle manquante (401)

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "API key is required"
}
```

### Cle invalide (401)

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid API key"
}
```

### Cle revoquee (403)

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "API key has been revoked"
}
```

## Bonnes pratiques

1. **Ne jamais commiter** les API Keys dans le code
2. **Utiliser des variables d'environnement** pour stocker les cles
3. **Faire tourner regulierement** les cles (tous les 90 jours)
4. **Utiliser des cles differentes** par environnement (dev, staging, prod)
5. **Monitorer l'usage** des cles pour detecter les anomalies
6. **Revoquer immediatement** les cles compromises
