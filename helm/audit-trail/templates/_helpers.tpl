{{/*
Expand the name of the chart.
*/}}
{{- define "audit-trail.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "audit-trail.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "audit-trail.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "audit-trail.labels" -}}
helm.sh/chart: {{ include "audit-trail.chart" . }}
{{ include "audit-trail.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "audit-trail.selectorLabels" -}}
app.kubernetes.io/name: {{ include "audit-trail.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "audit-trail.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "audit-trail.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
PostgreSQL host
*/}}
{{- define "audit-trail.postgresql.host" -}}
{{- if .Values.postgresql.enabled }}
{{- printf "%s-postgresql" .Release.Name }}
{{- else }}
{{- .Values.externalDatabase.host }}
{{- end }}
{{- end }}

{{/*
PostgreSQL port
*/}}
{{- define "audit-trail.postgresql.port" -}}
{{- if .Values.postgresql.enabled }}
{{- 5432 }}
{{- else }}
{{- .Values.externalDatabase.port }}
{{- end }}
{{- end }}

{{/*
PostgreSQL database
*/}}
{{- define "audit-trail.postgresql.database" -}}
{{- if .Values.postgresql.enabled }}
{{- .Values.postgresql.auth.database }}
{{- else }}
{{- .Values.externalDatabase.database }}
{{- end }}
{{- end }}

{{/*
Elasticsearch host
*/}}
{{- define "audit-trail.elasticsearch.host" -}}
{{- if .Values.elasticsearch.enabled }}
{{- printf "%s-elasticsearch" .Release.Name }}
{{- else }}
{{- .Values.externalElasticsearch.host }}
{{- end }}
{{- end }}

{{/*
Elasticsearch port
*/}}
{{- define "audit-trail.elasticsearch.port" -}}
{{- if .Values.elasticsearch.enabled }}
{{- 9200 }}
{{- else }}
{{- .Values.externalElasticsearch.port }}
{{- end }}
{{- end }}

{{/*
Redis host
*/}}
{{- define "audit-trail.redis.host" -}}
{{- if .Values.redis.enabled }}
{{- printf "%s-redis-master" .Release.Name }}
{{- else }}
{{- .Values.externalRedis.host }}
{{- end }}
{{- end }}

{{/*
Redis port
*/}}
{{- define "audit-trail.redis.port" -}}
{{- if .Values.redis.enabled }}
{{- 6379 }}
{{- else }}
{{- .Values.externalRedis.port }}
{{- end }}
{{- end }}

{{/*
Kafka bootstrap servers
*/}}
{{- define "audit-trail.kafka.bootstrapServers" -}}
{{- if .Values.kafka.enabled }}
{{- printf "%s-kafka:9092" .Release.Name }}
{{- else }}
{{- .Values.externalKafka.bootstrapServers }}
{{- end }}
{{- end }}
