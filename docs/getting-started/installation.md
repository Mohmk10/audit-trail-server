# Installation

Guide d'installation detaille pour Audit Trail.

## Prerequisites

### Requis

- Java 21+ (pour build from source)
- Docker 24+ et Docker Compose v2
- PostgreSQL 15+ (ou via Docker)
- Elasticsearch 8.x (ou via Docker)

### Recommandes

- Redis 7+ (pour cache et rate limiting)
- Kafka 3.x (pour streaming d'evenements)

## Options d'installation

### Option 1 : Docker Compose (Recommande)

La methode la plus simple pour demarrer rapidement.

```bash
# Cloner le repository
git clone https://github.com/Mohmk10/audit-trail-server.git
cd audit-trail-server

# Demarrer tous les services
docker-compose -f docker/docker-compose.yml up -d
```

Services demarres :
- **audit-trail-server** : Application principale (port 8080)
- **postgres** : Base de donnees (port 5432)
- **elasticsearch** : Moteur de recherche (port 9200)
- **redis** : Cache (port 6379)
- **kafka** + **zookeeper** : Message broker (port 9092)

### Option 2 : Docker seul

Si vous avez deja PostgreSQL et Elasticsearch.

```bash
docker run -d \
  --name audit-trail-server \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-postgres:5432/audittrail \
  -e SPRING_DATASOURCE_USERNAME=audittrail \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  -e SPRING_ELASTICSEARCH_URIS=http://your-elasticsearch:9200 \
  devmohmk/audit-trail-server:latest
```

### Option 3 : Kubernetes avec Helm

Pour les deploiements production.

```bash
# Ajouter le repo Helm
helm repo add audit-trail https://Mohmk10.github.io/audit-trail-server
helm repo update

# Installer avec les valeurs par defaut
helm install my-audit-trail audit-trail/audit-trail

# Ou avec un fichier de configuration
helm install my-audit-trail audit-trail/audit-trail -f my-values.yaml
```

Voir [Deploiement Kubernetes](../deployment/kubernetes.md) pour plus de details.

### Option 4 : Build from Source

Pour le developpement ou la personnalisation.

```bash
# Cloner
git clone https://github.com/Mohmk10/audit-trail-server.git
cd audit-trail-server

# Build
mvn clean package -DskipTests

# Le JAR est dans audit-trail-app/target/
java -jar audit-trail-app/target/audit-trail-app-1.0.0.jar
```

## Configuration des services externes

### PostgreSQL

```sql
-- Creer la base de donnees
CREATE DATABASE audittrail;

-- Creer l'utilisateur
CREATE USER audittrail WITH PASSWORD 'your-secure-password';

-- Accorder les privileges
GRANT ALL PRIVILEGES ON DATABASE audittrail TO audittrail;
```

Les migrations Flyway sont appliquees automatiquement au demarrage.

### Elasticsearch

Aucune configuration speciale requise. Les index sont crees automatiquement.

Configuration recommandee pour la production :

```yaml
# elasticsearch.yml
cluster.name: audit-trail
node.name: node-1
network.host: 0.0.0.0
discovery.type: single-node  # ou cluster pour HA

# JVM heap (50% de la RAM disponible, max 32GB)
ES_JAVA_OPTS: "-Xms4g -Xmx4g"
```

### Redis (Optionnel)

```bash
# Docker
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Configuration
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
```

### Kafka (Optionnel)

```bash
# Activer Kafka
AUDIT_TRAIL_KAFKA_ENABLED=true
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Verification de l'installation

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

Reponse attendue :
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "elasticsearch": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

### Metriques

```bash
curl http://localhost:8080/actuator/prometheus
```

### Info

```bash
curl http://localhost:8080/actuator/info
```

## Troubleshooting

### L'application ne demarre pas

1. Verifiez que PostgreSQL est accessible
2. Verifiez que Elasticsearch est accessible
3. Consultez les logs : `docker logs audit-trail-server`

### Erreur de connexion a la base

```
Connection refused to localhost:5432
```

Verifiez l'URL de connexion et les credentials.

### Elasticsearch non disponible

```
NoNodeAvailableException
```

Verifiez que Elasticsearch est demarre et accessible.

## Prochaines etapes

- [Configuration](configuration.md) - Personnaliser la configuration
- [Quick Start](quick-start.md) - Premier evenement
