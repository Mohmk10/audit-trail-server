# Production Deployment Guide

Guide complet pour le deploiement en production.

## Checklist Pre-Production

### Infrastructure

- [ ] Cluster Kubernetes configure avec haute disponibilite
- [ ] Ingress controller installe (nginx/traefik)
- [ ] Cert-manager configure pour TLS
- [ ] Storage class avec performance adequat
- [ ] Monitoring stack deploye (Prometheus/Grafana)
- [ ] Log aggregation configure (ELK/Loki)

### Securite

- [ ] Network policies definies
- [ ] Pod security policies/standards appliquees
- [ ] Secrets geres via vault/external-secrets
- [ ] RBAC configure
- [ ] TLS active sur tous les endpoints
- [ ] Rate limiting configure

### Application

- [ ] Tests de charge effectues
- [ ] Health checks configures
- [ ] Resource limits definis
- [ ] HPA configure
- [ ] Backup strategy definie
- [ ] Disaster recovery plan

## Architecture de Reference

```
                         ┌─────────────────────────────────────┐
                         │            Load Balancer            │
                         │         (Cloud Provider)            │
                         └──────────────┬──────────────────────┘
                                        │
                         ┌──────────────┴──────────────────────┐
                         │         Ingress Controller           │
                         │            (NGINX)                   │
                         └──────────────┬──────────────────────┘
                                        │
         ┌──────────────────────────────┼──────────────────────────────┐
         │                              │                              │
         ▼                              ▼                              ▼
┌─────────────────┐          ┌─────────────────┐          ┌─────────────────┐
│   Audit Trail   │          │   Audit Trail   │          │   Audit Trail   │
│     Pod 1       │          │     Pod 2       │          │     Pod 3       │
│   (Zone A)      │          │   (Zone B)      │          │   (Zone C)      │
└────────┬────────┘          └────────┬────────┘          └────────┬────────┘
         │                            │                            │
         └────────────────────────────┼────────────────────────────┘
                                      │
         ┌────────────────────────────┼────────────────────────────┐
         │                            │                            │
         ▼                            ▼                            ▼
┌─────────────────┐          ┌─────────────────┐          ┌─────────────────┐
│   PostgreSQL    │          │  Elasticsearch  │          │     Redis       │
│   (Primary +    │          │   (3 nodes)     │          │   (Sentinel)    │
│    Replicas)    │          │                 │          │                 │
└─────────────────┘          └─────────────────┘          └─────────────────┘
```

## Configuration Production

### values-prod.yaml

```yaml
# Replicas et scaling
replicaCount: 3

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 20
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
        - type: Percent
          value: 10
          periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
        - type: Percent
          value: 100
          periodSeconds: 15
        - type: Pods
          value: 4
          periodSeconds: 15
      selectPolicy: Max

# Resources
resources:
  requests:
    cpu: 1000m
    memory: 2Gi
  limits:
    cpu: 4000m
    memory: 8Gi

# JVM options
env:
  - name: JAVA_OPTS
    value: >-
      -Xms2g
      -Xmx6g
      -XX:+UseG1GC
      -XX:MaxGCPauseMillis=200
      -XX:+UseStringDeduplication
      -XX:+HeapDumpOnOutOfMemoryError
      -XX:HeapDumpPath=/tmp/heapdump.hprof

# Probes
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3

# Affinity
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      - labelSelector:
          matchLabels:
            app.kubernetes.io/name: audit-trail
        topologyKey: kubernetes.io/hostname
  nodeAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 100
        preference:
          matchExpressions:
            - key: node-type
              operator: In
              values:
                - compute-optimized

# Topology spread
topologySpreadConstraints:
  - maxSkew: 1
    topologyKey: topology.kubernetes.io/zone
    whenUnsatisfiable: DoNotSchedule
    labelSelector:
      matchLabels:
        app.kubernetes.io/name: audit-trail

# Ingress
ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/rate-limit: "1000"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "60"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "60"
  hosts:
    - host: audit-api.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: audit-trail-tls
      hosts:
        - audit-api.example.com

# PostgreSQL
postgresql:
  enabled: true
  architecture: replication
  auth:
    postgresPassword: "" # Utiliser secret externe
    existingSecret: postgresql-secrets
  primary:
    resources:
      requests:
        cpu: 2000m
        memory: 4Gi
      limits:
        cpu: 4000m
        memory: 8Gi
    persistence:
      size: 500Gi
      storageClass: fast-ssd
  readReplicas:
    replicaCount: 2
    resources:
      requests:
        cpu: 1000m
        memory: 2Gi
  metrics:
    enabled: true

# Elasticsearch
elasticsearch:
  enabled: true
  replicas: 3
  minimumMasterNodes: 2
  clusterHealthCheckParams: "wait_for_status=yellow&timeout=1s"
  resources:
    requests:
      cpu: 2000m
      memory: 4Gi
    limits:
      cpu: 4000m
      memory: 8Gi
  volumeClaimTemplate:
    resources:
      requests:
        storage: 500Gi
    storageClassName: fast-ssd
  esConfig:
    elasticsearch.yml: |
      cluster.name: audit-trail
      xpack.security.enabled: true
      xpack.security.transport.ssl.enabled: true

# Redis
redis:
  enabled: true
  architecture: replication
  auth:
    existingSecret: redis-secrets
  replica:
    replicaCount: 2
    resources:
      requests:
        cpu: 500m
        memory: 1Gi
  sentinel:
    enabled: true
    masterSet: mymaster
  metrics:
    enabled: true

# Kafka
kafka:
  enabled: true
  replicaCount: 3
  resources:
    requests:
      cpu: 1000m
      memory: 2Gi
  persistence:
    size: 200Gi
  metrics:
    kafka:
      enabled: true
    jmx:
      enabled: true
```

## Securite

### Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: audit-trail-network-policy
  namespace: audit-trail
spec:
  podSelector:
    matchLabels:
      app.kubernetes.io/name: audit-trail
  policyTypes:
    - Ingress
    - Egress
  ingress:
    - from:
        - namespaceSelector:
            matchLabels:
              name: ingress-nginx
      ports:
        - protocol: TCP
          port: 8080
  egress:
    - to:
        - podSelector:
            matchLabels:
              app.kubernetes.io/name: postgresql
      ports:
        - protocol: TCP
          port: 5432
    - to:
        - podSelector:
            matchLabels:
              app: elasticsearch
      ports:
        - protocol: TCP
          port: 9200
    - to:
        - podSelector:
            matchLabels:
              app.kubernetes.io/name: redis
      ports:
        - protocol: TCP
          port: 6379
    - to:
        - podSelector:
            matchLabels:
              app.kubernetes.io/name: kafka
      ports:
        - protocol: TCP
          port: 9092
```

### Pod Security Standards

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: audit-trail
  labels:
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/audit: restricted
    pod-security.kubernetes.io/warn: restricted
```

### Security Context

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  runAsGroup: 1001
  fsGroup: 1001
  seccompProfile:
    type: RuntimeDefault

containerSecurityContext:
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: true
  capabilities:
    drop:
      - ALL
```

## Observabilite

### Prometheus ServiceMonitor

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: audit-trail
  labels:
    release: prometheus
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: audit-trail
  endpoints:
    - port: http
      path: /actuator/prometheus
      interval: 15s
      scrapeTimeout: 10s
```

### Alerting Rules

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: audit-trail-alerts
spec:
  groups:
    - name: audit-trail
      rules:
        - alert: HighErrorRate
          expr: |
            sum(rate(http_server_requests_seconds_count{status=~"5..",app="audit-trail"}[5m]))
            /
            sum(rate(http_server_requests_seconds_count{app="audit-trail"}[5m]))
            > 0.01
          for: 5m
          labels:
            severity: critical
          annotations:
            summary: High error rate on Audit Trail API
            description: Error rate is {{ $value | humanizePercentage }}

        - alert: HighLatency
          expr: |
            histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket{app="audit-trail"}[5m])) by (le))
            > 1
          for: 5m
          labels:
            severity: warning
          annotations:
            summary: High latency on Audit Trail API
            description: P99 latency is {{ $value | humanizeDuration }}

        - alert: PodNotReady
          expr: |
            kube_pod_status_ready{namespace="audit-trail",condition="true"} == 0
          for: 5m
          labels:
            severity: warning
          annotations:
            summary: Pod {{ $labels.pod }} is not ready

        - alert: DatabaseConnectionPoolExhausted
          expr: |
            hikaricp_connections_active{app="audit-trail"}
            /
            hikaricp_connections_max{app="audit-trail"}
            > 0.9
          for: 5m
          labels:
            severity: critical
          annotations:
            summary: Database connection pool nearly exhausted
```

### Logging Configuration

```yaml
# logback-spring.xml pour production
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdc>true</includeMdc>
            <customFields>{"app":"audit-trail","env":"production"}</customFields>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="JSON" />
    </root>

    <logger name="com.mohmk10.audittrail" level="INFO" />
    <logger name="org.springframework" level="WARN" />
    <logger name="org.hibernate" level="WARN" />
</configuration>
```

## Performance Tuning

### PostgreSQL

```sql
-- postgresql.conf optimizations
max_connections = 200
shared_buffers = 4GB
effective_cache_size = 12GB
maintenance_work_mem = 1GB
checkpoint_completion_target = 0.9
wal_buffers = 64MB
default_statistics_target = 100
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 20MB
min_wal_size = 1GB
max_wal_size = 4GB
max_worker_processes = 8
max_parallel_workers_per_gather = 4
max_parallel_workers = 8
max_parallel_maintenance_workers = 4
```

### Elasticsearch

```yaml
# elasticsearch.yml
cluster.name: audit-trail-prod
node.name: ${HOSTNAME}

# Memory
bootstrap.memory_lock: true

# Indexing
index.refresh_interval: 5s
index.translog.durability: async
index.translog.sync_interval: 5s

# Thread pools
thread_pool:
  write:
    size: 8
    queue_size: 1000
  search:
    size: 12
    queue_size: 1000

# Circuit breakers
indices.breaker.total.limit: 70%
```

### JVM Tuning

```bash
JAVA_OPTS="-Xms4g -Xmx4g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -XX:+ParallelRefProcEnabled \
    -XX:+DisableExplicitGC \
    -XX:+AlwaysPreTouch \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom"
```

## Disaster Recovery

### Backup Strategy

| Composant | Frequence | Retention | Methode |
|-----------|-----------|-----------|---------|
| PostgreSQL | Hourly | 7 days | pg_dump + WAL |
| Elasticsearch | Daily | 30 days | Snapshot |
| Redis | Hourly | 24 hours | RDB + AOF |
| Kafka | Continuous | 7 days | Log retention |

### Recovery Procedures

```bash
# Restore PostgreSQL
pg_restore -h $PG_HOST -U $PG_USER -d audittrail backup.dump

# Restore Elasticsearch
curl -X POST "localhost:9200/_snapshot/backup/snapshot_1/_restore"

# Restore Redis
redis-cli SLAVEOF NO ONE
redis-cli DEBUG RELOAD
```

### RTO/RPO Targets

| Scenario | RTO | RPO |
|----------|-----|-----|
| Pod failure | < 1 min | 0 |
| Node failure | < 5 min | 0 |
| Zone failure | < 10 min | 0 |
| Region failure | < 1 hour | < 1 hour |
| Data corruption | < 4 hours | < 1 hour |

## Go-Live Checklist

### J-7

- [ ] Load testing complete
- [ ] Security audit complete
- [ ] Runbooks documented
- [ ] On-call rotation setup
- [ ] Alerting configured

### J-1

- [ ] Final deployment to staging
- [ ] Smoke tests passing
- [ ] Rollback procedure tested
- [ ] Communication plan ready

### D-Day

- [ ] Deploy to production
- [ ] Smoke tests in production
- [ ] Monitor metrics closely
- [ ] Gradual traffic increase
- [ ] Document any issues

### J+1

- [ ] Review metrics
- [ ] Address any issues
- [ ] Update documentation
- [ ] Retrospective
