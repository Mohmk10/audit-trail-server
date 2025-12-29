# Configuration

Guide complet de configuration d'Audit Trail.

## Variables d'environnement

### Base de donnees (PostgreSQL)

| Variable | Description | Defaut |
|----------|-------------|--------|
| `SPRING_DATASOURCE_URL` | URL JDBC | `jdbc:postgresql://localhost:5432/audittrail` |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur | `audittrail` |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe | `audittrail` |
| `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE` | Taille du pool | `10` |

### Elasticsearch

| Variable | Description | Defaut |
|----------|-------------|--------|
| `SPRING_ELASTICSEARCH_URIS` | URL(s) Elasticsearch | `http://localhost:9200` |
| `SPRING_ELASTICSEARCH_USERNAME` | Utilisateur (si securise) | - |
| `SPRING_ELASTICSEARCH_PASSWORD` | Mot de passe (si securise) | - |

### Redis

| Variable | Description | Defaut |
|----------|-------------|--------|
| `SPRING_DATA_REDIS_HOST` | Host Redis | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Port | `6379` |
| `SPRING_DATA_REDIS_PASSWORD` | Mot de passe | - |

### Kafka

| Variable | Description | Defaut |
|----------|-------------|--------|
| `AUDIT_TRAIL_KAFKA_ENABLED` | Activer Kafka | `false` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Serveurs Kafka | `localhost:9092` |
| `AUDIT_TRAIL_KAFKA_TOPIC` | Topic principal | `audit-events` |

### Webhooks

| Variable | Description | Defaut |
|----------|-------------|--------|
| `AUDIT_TRAIL_WEBHOOK_ENABLED` | Activer webhooks | `true` |
| `AUDIT_TRAIL_WEBHOOK_TIMEOUT_SECONDS` | Timeout delivery | `30` |
| `AUDIT_TRAIL_WEBHOOK_MAX_RETRIES` | Nombre de retries | `5` |

### SIEM Export

| Variable | Description | Defaut |
|----------|-------------|--------|
| `AUDIT_TRAIL_SPLUNK_ENABLED` | Activer Splunk | `false` |
| `AUDIT_TRAIL_SPLUNK_URL` | URL HEC Splunk | - |
| `AUDIT_TRAIL_SPLUNK_TOKEN` | Token HEC | - |
| `AUDIT_TRAIL_ELK_ENABLED` | Activer ELK export | `false` |
| `AUDIT_TRAIL_ELK_URL` | URL Elasticsearch | - |
| `AUDIT_TRAIL_S3_ENABLED` | Activer S3 | `false` |
| `AUDIT_TRAIL_S3_BUCKET` | Bucket S3 | - |
| `AUDIT_TRAIL_S3_REGION` | Region AWS | - |

### JVM

| Variable | Description | Recommande |
|----------|-------------|------------|
| `JAVA_OPTS` | Options JVM | `-Xmx512m -XX:+UseG1GC` |

## Configuration YAML

Vous pouvez aussi utiliser un fichier `application.yml` :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/audittrail
    username: audittrail
    password: secret
    hikari:
      maximum-pool-size: 20

  elasticsearch:
    uris: http://localhost:9200

  data:
    redis:
      host: localhost
      port: 6379

  kafka:
    bootstrap-servers: localhost:9092

audit-trail:
  kafka:
    enabled: true
    topic: audit-events

  webhook:
    enabled: true
    timeout-seconds: 30
    max-retries: 5
    retry-delays: [60, 300, 900, 3600, 14400]

  splunk:
    enabled: false
    url: https://splunk.example.com:8088
    token: your-hec-token
    index: audit_events

  elk:
    enabled: false
    url: http://elasticsearch:9200
    index-prefix: audit-events

  s3:
    enabled: false
    bucket: audit-trail-archive
    region: us-east-1
    prefix: events/

server:
  port: 8080

logging:
  level:
    com.mohmk10.audittrail: INFO
    org.springframework: WARN
```

## Profils Spring

### Profils disponibles

- `default` - Configuration locale
- `docker` - Configuration Docker
- `kubernetes` - Configuration Kubernetes
- `production` - Configuration production

### Utilisation

```bash
# Via variable d'environnement
SPRING_PROFILES_ACTIVE=production

# Via ligne de commande
java -jar app.jar --spring.profiles.active=production
```

## Configuration par environnement

### Developpement

```yaml
spring:
  profiles:
    active: default

logging:
  level:
    com.mohmk10.audittrail: DEBUG
```

### Production

```yaml
spring:
  profiles:
    active: production

server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}

logging:
  level:
    com.mohmk10.audittrail: INFO
    org.springframework: WARN
```

## Securite

### TLS/SSL

```yaml
server:
  ssl:
    enabled: true
    key-store: /path/to/keystore.p12
    key-store-password: your-password
    key-store-type: PKCS12
```

### Rate Limiting

```yaml
audit-trail:
  rate-limiting:
    enabled: true
    requests-per-minute: 1000
```

## Monitoring

### Actuator Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
```

Endpoints disponibles :
- `/actuator/health` - Health check
- `/actuator/info` - Informations application
- `/actuator/metrics` - Metriques
- `/actuator/prometheus` - Format Prometheus

## Prochaines etapes

- [API Reference](../api/overview.md) - Documentation API
- [Deploiement](../deployment/docker.md) - Guide de deploiement
