# Quick Start

Demarrez avec Audit Trail en 5 minutes.

## Prerequisites

- Docker et Docker Compose installes
- curl ou un client HTTP

## 1. Demarrer le serveur

```bash
# Cloner le repository
git clone https://github.com/Mohmk10/audit-trail-server.git
cd audit-trail-server

# Demarrer la stack complete
docker-compose -f docker/docker-compose.yml up -d

# Attendre que les services soient prets (~60 secondes)
sleep 60

# Verifier le statut
curl http://localhost:8080/actuator/health
```

Reponse attendue :
```json
{
  "status": "UP"
}
```

## 2. Creer un tenant et une API Key

```bash
# Creer un tenant
curl -X POST http://localhost:8080/api/v1/admin/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "id": "my-tenant",
    "name": "My Company",
    "plan": "STANDARD"
  }'

# Creer une source
curl -X POST http://localhost:8080/api/v1/admin/sources \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "my-tenant",
    "name": "My Application",
    "description": "Main web application"
  }'

# Creer une API Key
curl -X POST http://localhost:8080/api/v1/admin/api-keys \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "my-tenant",
    "sourceId": "source-id-from-above",
    "name": "Production Key"
  }'
```

Notez la valeur `key` retournee - c'est votre API Key.

## 3. Envoyer votre premier evenement

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -d '{
    "actor": {
      "id": "user-123",
      "type": "USER",
      "name": "John Doe",
      "ip": "192.168.1.100"
    },
    "action": {
      "type": "LOGIN",
      "description": "User logged in",
      "category": "AUTHENTICATION"
    },
    "resource": {
      "id": "session-456",
      "type": "SESSION",
      "name": "Web Session"
    },
    "metadata": {
      "source": "web-app",
      "tenantId": "my-tenant"
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

## 4. Rechercher des evenements

```bash
# Recherche rapide
curl "http://localhost:8080/api/v1/search/quick?q=login&tenantId=my-tenant" \
  -H "X-API-Key: YOUR_API_KEY"

# Recherche avancee
curl -X POST http://localhost:8080/api/v1/search \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -d '{
    "tenantId": "my-tenant",
    "actorId": "user-123",
    "fromDate": "2025-01-01T00:00:00Z",
    "toDate": "2025-12-31T23:59:59Z"
  }'
```

## 5. Generer un rapport

```bash
# Demander un rapport PDF
curl -X POST http://localhost:8080/api/v1/reports \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -d '{
    "tenantId": "my-tenant",
    "format": "PDF",
    "title": "Audit Report January 2025",
    "fromDate": "2025-01-01T00:00:00Z",
    "toDate": "2025-01-31T23:59:59Z"
  }'

# Telecharger le rapport (avec l'ID retourne)
curl -O http://localhost:8080/api/v1/reports/{report-id}/download \
  -H "X-API-Key: YOUR_API_KEY"
```

## Prochaines etapes

- [Installation detaillee](installation.md) - Options d'installation
- [Configuration](configuration.md) - Personnaliser la configuration
- [API Reference](../api/overview.md) - Documentation complete de l'API
- [SDKs](../sdks/overview.md) - Integrer avec votre langage prefere

## Arreter les services

```bash
docker-compose -f docker/docker-compose.yml down
```

Pour supprimer aussi les donnees :

```bash
docker-compose -f docker/docker-compose.yml down -v
```
