# Kubernetes Deployment

Guide de deploiement sur Kubernetes avec Helm.

## Prerequisites

- Kubernetes cluster 1.25+
- Helm 3.10+
- kubectl configure
- Ingress controller (nginx, traefik)
- Storage class pour les volumes persistants

## Quick Start avec Helm

```bash
# Ajouter le repo Helm
helm repo add audit-trail https://mohmk10.github.io/audit-trail-server
helm repo update

# Installer avec les valeurs par defaut
helm install audit-trail audit-trail/audit-trail

# Installer avec configuration personnalisee
helm install audit-trail audit-trail/audit-trail \
    -f values-prod.yaml \
    --namespace audit-trail \
    --create-namespace
```

## Chart Structure

```
helm/audit-trail/
├── Chart.yaml
├── values.yaml
├── values-prod.yaml
├── templates/
│   ├── _helpers.tpl
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── ingress.yaml
│   ├── hpa.yaml
│   └── serviceaccount.yaml
└── charts/
    ├── postgresql/
    ├── elasticsearch/
    ├── redis/
    └── kafka/
```

## Configuration (values.yaml)

### Minimal

```yaml
# values.yaml
replicaCount: 2

image:
  repository: devmohmk/audit-trail-server
  tag: "1.0.0"

ingress:
  enabled: true
  hosts:
    - host: audit.example.com
      paths:
        - path: /
          pathType: Prefix

postgresql:
  enabled: true
  auth:
    password: "change-me"

elasticsearch:
  enabled: true

redis:
  enabled: true
```

### Production

```yaml
# values-prod.yaml
replicaCount: 3

image:
  repository: devmohmk/audit-trail-server
  tag: "1.0.0"
  pullPolicy: IfNotPresent

resources:
  requests:
    cpu: 500m
    memory: 1Gi
  limits:
    cpu: 2000m
    memory: 4Gi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/rate-limit: "100"
  hosts:
    - host: audit.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: audit-trail-tls
      hosts:
        - audit.example.com

postgresql:
  enabled: true
  architecture: replication
  auth:
    password: "${POSTGRES_PASSWORD}"
  primary:
    resources:
      requests:
        cpu: 500m
        memory: 1Gi
  readReplicas:
    replicaCount: 2
  persistence:
    size: 100Gi
    storageClass: fast-ssd

elasticsearch:
  enabled: true
  replicas: 3
  minimumMasterNodes: 2
  resources:
    requests:
      cpu: 1000m
      memory: 2Gi
  volumeClaimTemplate:
    resources:
      requests:
        storage: 100Gi
    storageClassName: fast-ssd

redis:
  enabled: true
  architecture: replication
  replica:
    replicaCount: 2

kafka:
  enabled: true
  replicaCount: 3
```

## Manifests Kubernetes

### Deployment

```yaml
# templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "audit-trail.fullname" . }}
  labels:
    {{- include "audit-trail.labels" . | nindent 4 }}
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  selector:
    matchLabels:
      {{- include "audit-trail.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "audit-trail.selectorLabels" . | nindent 8 }}
    spec:
      serviceAccountName: {{ include "audit-trail.serviceAccountName" . }}
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        fsGroup: 1001
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: 8080
              protocol: TCP
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: kubernetes
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                configMapKeyRef:
                  name: {{ include "audit-trail.fullname" . }}
                  key: database-url
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: {{ include "audit-trail.fullname" . }}
                  key: database-password
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            initialDelaySeconds: 60
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: http
            initialDelaySeconds: 30
            periodSeconds: 5
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
```

### Service

```yaml
# templates/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: {{ include "audit-trail.fullname" . }}
  labels:
    {{- include "audit-trail.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.port }}
      targetPort: http
      protocol: TCP
      name: http
  selector:
    {{- include "audit-trail.selectorLabels" . | nindent 4 }}
```

### HorizontalPodAutoscaler

```yaml
# templates/hpa.yaml
{{- if .Values.autoscaling.enabled }}
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: {{ include "audit-trail.fullname" . }}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: {{ include "audit-trail.fullname" . }}
  minReplicas: {{ .Values.autoscaling.minReplicas }}
  maxReplicas: {{ .Values.autoscaling.maxReplicas }}
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: {{ .Values.autoscaling.targetCPUUtilizationPercentage }}
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: {{ .Values.autoscaling.targetMemoryUtilizationPercentage }}
{{- end }}
```

### Ingress

```yaml
# templates/ingress.yaml
{{- if .Values.ingress.enabled }}
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: {{ include "audit-trail.fullname" . }}
  annotations:
    {{- toYaml .Values.ingress.annotations | nindent 4 }}
spec:
  ingressClassName: {{ .Values.ingress.className }}
  {{- if .Values.ingress.tls }}
  tls:
    {{- toYaml .Values.ingress.tls | nindent 4 }}
  {{- end }}
  rules:
    {{- range .Values.ingress.hosts }}
    - host: {{ .host }}
      http:
        paths:
          {{- range .paths }}
          - path: {{ .path }}
            pathType: {{ .pathType }}
            backend:
              service:
                name: {{ include "audit-trail.fullname" $ }}
                port:
                  number: 8080
          {{- end }}
    {{- end }}
{{- end }}
```

## Commands Helm

### Installation

```bash
# Installation basique
helm install audit-trail ./helm/audit-trail

# Installation dans un namespace
helm install audit-trail ./helm/audit-trail \
    --namespace audit-trail \
    --create-namespace

# Installation avec valeurs personnalisees
helm install audit-trail ./helm/audit-trail \
    -f values-prod.yaml \
    --set image.tag=1.0.0

# Installation dry-run
helm install audit-trail ./helm/audit-trail --dry-run --debug
```

### Mise a jour

```bash
# Mise a jour
helm upgrade audit-trail ./helm/audit-trail

# Mise a jour avec nouvelle image
helm upgrade audit-trail ./helm/audit-trail \
    --set image.tag=1.1.0

# Rollback
helm rollback audit-trail 1
```

### Gestion

```bash
# Liste des releases
helm list -n audit-trail

# Statut
helm status audit-trail -n audit-trail

# Historique
helm history audit-trail -n audit-trail

# Desinstallation
helm uninstall audit-trail -n audit-trail
```

## kubectl Commands

### Verification

```bash
# Pods
kubectl get pods -n audit-trail
kubectl describe pod audit-trail-xxx -n audit-trail

# Services
kubectl get svc -n audit-trail

# Ingress
kubectl get ingress -n audit-trail

# HPA
kubectl get hpa -n audit-trail
```

### Logs

```bash
# Logs d'un pod
kubectl logs -f audit-trail-xxx -n audit-trail

# Logs de tous les pods
kubectl logs -f -l app.kubernetes.io/name=audit-trail -n audit-trail

# Logs precedents
kubectl logs audit-trail-xxx --previous -n audit-trail
```

### Debug

```bash
# Shell dans un pod
kubectl exec -it audit-trail-xxx -n audit-trail -- /bin/sh

# Port-forward
kubectl port-forward svc/audit-trail 8080:8080 -n audit-trail

# Events
kubectl get events -n audit-trail --sort-by='.lastTimestamp'
```

## Monitoring

### Prometheus

```yaml
# ServiceMonitor
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: audit-trail
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: audit-trail
  endpoints:
    - port: http
      path: /actuator/prometheus
      interval: 15s
```

### Grafana Dashboard

```bash
# Importer le dashboard
kubectl apply -f https://raw.githubusercontent.com/devmohmk/audit-trail-server/main/monitoring/grafana-dashboard.yaml
```

## Secrets Management

### Kubernetes Secrets

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: audit-trail-secrets
type: Opaque
stringData:
  database-password: "secure-password"
  api-key: "secret-api-key"
```

### External Secrets Operator

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: audit-trail-secrets
spec:
  refreshInterval: 1h
  secretStoreRef:
    kind: ClusterSecretStore
    name: vault
  target:
    name: audit-trail-secrets
  data:
    - secretKey: database-password
      remoteRef:
        key: audit-trail/database
        property: password
```

### Sealed Secrets

```bash
# Creer un sealed secret
kubeseal --format yaml < secret.yaml > sealed-secret.yaml
kubectl apply -f sealed-secret.yaml
```

## High Availability

### Pod Disruption Budget

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: audit-trail-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app.kubernetes.io/name: audit-trail
```

### Anti-Affinity

```yaml
# Dans le deployment
spec:
  template:
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                labelSelector:
                  matchLabels:
                    app.kubernetes.io/name: audit-trail
                topologyKey: kubernetes.io/hostname
```

### Topology Spread

```yaml
spec:
  template:
    spec:
      topologySpreadConstraints:
        - maxSkew: 1
          topologyKey: topology.kubernetes.io/zone
          whenUnsatisfiable: ScheduleAnyway
          labelSelector:
            matchLabels:
              app.kubernetes.io/name: audit-trail
```

## Backup & Restore

### Velero

```bash
# Backup
velero backup create audit-trail-backup \
    --include-namespaces audit-trail \
    --include-resources deployments,configmaps,secrets,pvc

# Restore
velero restore create --from-backup audit-trail-backup
```

### Database Backup

```bash
# CronJob pour backup PostgreSQL
kubectl apply -f - <<EOF
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
spec:
  schedule: "0 2 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: backup
              image: postgres:15
              command:
                - /bin/sh
                - -c
                - pg_dump -h postgresql -U audittrail audittrail | gzip > /backup/backup-\$(date +%Y%m%d).sql.gz
              volumeMounts:
                - name: backup
                  mountPath: /backup
          volumes:
            - name: backup
              persistentVolumeClaim:
                claimName: backup-pvc
          restartPolicy: OnFailure
EOF
```
