# Docker Deployment

Guide de deploiement avec Docker.

## Quick Start

```bash
# Cloner le repository
git clone https://github.com/devmohmk/audit-trail-server.git
cd audit-trail-server

# Demarrer avec Docker Compose
docker-compose up -d

# Verifier le statut
docker-compose ps

# Voir les logs
docker-compose logs -f audit-trail
```

L'API sera disponible sur `http://localhost:8080`.

## Docker Compose

### Configuration complete

```yaml
# docker-compose.yml
version: '3.8'

services:
  audit-trail:
    image: devmohmk/audit-trail-server:latest
    container_name: audit-trail
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/audittrail
      - SPRING_DATASOURCE_USERNAME=audittrail
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200
      - SPRING_REDIS_HOST=redis
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      postgres:
        condition: service_healthy
      elasticsearch:
        condition: service_healthy
      redis:
        condition: service_started
      kafka:
        condition: service_started
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - audit-trail-network

  postgres:
    image: postgres:15-alpine
    container_name: audit-trail-postgres
    environment:
      - POSTGRES_DB=audittrail
      - POSTGRES_USER=audittrail
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U audittrail"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - audit-trail-network

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: audit-trail-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
    networks:
      - audit-trail-network

  redis:
    image: redis:7-alpine
    container_name: audit-trail-redis
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    networks:
      - audit-trail-network

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: audit-trail-kafka
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
    depends_on:
      - zookeeper
    networks:
      - audit-trail-network

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: audit-trail-zookeeper
    environment:
      - ZOOKEEPER_CLIENT_PORT=2181
    networks:
      - audit-trail-network

volumes:
  postgres_data:
  elasticsearch_data:
  redis_data:

networks:
  audit-trail-network:
    driver: bridge
```

### Fichier .env

```bash
# .env
DB_PASSWORD=your-secure-password
AUDIT_TRAIL_API_KEY=your-api-key
```

## Dockerfile

### Production

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as non-root
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy application
COPY --chown=appuser:appgroup target/audit-trail-app.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Multi-stage build

```dockerfile
# Dockerfile.build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
COPY */pom.xml ./
RUN mvn dependency:go-offline -B

# Build
COPY . .
RUN mvn clean package -DskipTests -B

# Runtime
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/audit-trail-app/target/audit-trail-app.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Development

```dockerfile
# Dockerfile.dev
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Cache dependencies
COPY pom.xml .
COPY */pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source
COPY . .

# Hot reload avec spring-boot-devtools
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]
```

## Commands Docker

### Build

```bash
# Build de l'image
docker build -t devmohmk/audit-trail-server:latest .

# Build avec tag specifique
docker build -t devmohmk/audit-trail-server:1.0.0 .

# Build multi-platform
docker buildx build --platform linux/amd64,linux/arm64 \
    -t devmohmk/audit-trail-server:latest \
    --push .
```

### Run

```bash
# Run simple
docker run -d -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=docker \
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/audittrail \
    devmohmk/audit-trail-server:latest

# Run avec volume pour logs
docker run -d -p 8080:8080 \
    -v /var/log/audit-trail:/app/logs \
    devmohmk/audit-trail-server:latest

# Run interactif pour debug
docker run -it --rm \
    -p 8080:8080 \
    devmohmk/audit-trail-server:latest
```

### Logs

```bash
# Voir les logs
docker logs audit-trail

# Suivre les logs
docker logs -f audit-trail

# Logs avec timestamps
docker logs -t audit-trail

# Derniers N logs
docker logs --tail 100 audit-trail
```

### Debug

```bash
# Shell dans le container
docker exec -it audit-trail /bin/sh

# Verifier les variables d'environnement
docker exec audit-trail env

# Verifier la memoire
docker stats audit-trail

# Inspecter le container
docker inspect audit-trail
```

## Configuration

### Variables d'environnement

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Profil Spring | `default` |
| `SPRING_DATASOURCE_URL` | URL PostgreSQL | - |
| `SPRING_DATASOURCE_USERNAME` | User PostgreSQL | - |
| `SPRING_DATASOURCE_PASSWORD` | Password PostgreSQL | - |
| `SPRING_ELASTICSEARCH_URIS` | URIs Elasticsearch | - |
| `SPRING_REDIS_HOST` | Host Redis | `localhost` |
| `SPRING_REDIS_PORT` | Port Redis | `6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Serveurs Kafka | - |
| `JAVA_OPTS` | Options JVM | - |

### JVM Options

```bash
docker run -d -p 8080:8080 \
    -e JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC" \
    devmohmk/audit-trail-server:latest
```

### Limites de ressources

```yaml
services:
  audit-trail:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '0.5'
          memory: 512M
```

## Volumes et Persistence

### PostgreSQL

```yaml
volumes:
  postgres_data:
    driver: local
    driver_opts:
      type: none
      device: /data/postgresql
      o: bind
```

### Elasticsearch

```yaml
volumes:
  elasticsearch_data:
    driver: local
    driver_opts:
      type: none
      device: /data/elasticsearch
      o: bind
```

### Backup

```bash
# Backup PostgreSQL
docker exec audit-trail-postgres pg_dump -U audittrail audittrail > backup.sql

# Restore PostgreSQL
docker exec -i audit-trail-postgres psql -U audittrail audittrail < backup.sql

# Backup volume
docker run --rm \
    -v postgres_data:/data \
    -v $(pwd):/backup \
    alpine tar czf /backup/postgres_backup.tar.gz -C /data .
```

## Networking

### Network personnalise

```yaml
networks:
  audit-trail-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

### Expose externe

```yaml
services:
  audit-trail:
    ports:
      - "8080:8080"      # HTTP
      - "8443:8443"      # HTTPS
      - "9090:9090"      # Metrics
```

## Health Checks

### Liveness

```bash
curl http://localhost:8080/actuator/health/liveness
```

### Readiness

```bash
curl http://localhost:8080/actuator/health/readiness
```

### Configuration

```yaml
services:
  audit-trail:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
```

## Troubleshooting

### Container ne demarre pas

```bash
# Verifier les logs
docker logs audit-trail

# Verifier l'etat
docker inspect audit-trail | jq '.[0].State'

# Verifier les dependances
docker-compose ps
```

### Problemes de connexion

```bash
# Tester la connectivite
docker exec audit-trail ping postgres
docker exec audit-trail nc -zv postgres 5432

# Verifier le reseau
docker network inspect audit-trail-network
```

### Problemes de memoire

```bash
# Verifier l'utilisation
docker stats audit-trail

# Augmenter les limites
docker update --memory 4g --memory-swap 4g audit-trail
```

### Problemes de permissions

```bash
# Verifier l'utilisateur
docker exec audit-trail id

# Corriger les permissions des volumes
docker exec -u root audit-trail chown -R appuser:appgroup /app/logs
```
