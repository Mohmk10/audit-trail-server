# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2025-01-XX

### Added

#### Core Module
- Event domain model with Actor, Action, Resource, and Metadata
- Immutable storage with SHA-256 hash chain
- ECDSA P-256 cryptographic signatures for event integrity
- Event validation with Jakarta Bean Validation

#### Storage Module
- PostgreSQL-based persistent storage
- Hash chain integrity verification
- Signature verification with public key cryptography
- JPA entities with optimized indexes

#### Ingestion Module
- Single event ingestion endpoint
- Batch event ingestion (up to 1000 events)
- Asynchronous processing with CompletableFuture
- Input validation and sanitization

#### Search Module
- Elasticsearch integration for full-text search
- Advanced search with multiple filters
- Quick search endpoint for simple queries
- Timeline view with chronological grouping
- Aggregations by actor, action type, resource type
- Pagination support

#### Reporting Module
- PDF report generation with iText
- CSV export with Apache Commons CSV
- Excel export with Apache POI
- JSON export
- Report certification with digital signatures
- Scheduled report generation
- Report templates

#### Detection Module
- Rule engine with multiple rule types:
  - Threshold rules (count-based)
  - Pattern rules (regex matching)
  - Sequence rules (event sequences)
  - Anomaly rules (statistical deviation)
  - Blacklist/Whitelist rules
- Real-time alert generation
- Notification channels:
  - Email notifications
  - Slack webhooks
  - Custom webhooks
  - Log output
- Alert lifecycle management (acknowledge, resolve, dismiss)
- Alert statistics and metrics

#### Admin Module
- Multi-tenant management with quotas
- Source registration and management
- API key generation with HMAC-SHA256 hashing
- API key rotation support
- User management with RBAC:
  - ADMIN role
  - AUDITOR role
  - VIEWER role
- Audit of audit (admin actions are logged)
- System health monitoring
- Pagination for all list endpoints

#### Integration Module
- Webhook delivery system:
  - HMAC-SHA256 signatures
  - Retry with exponential backoff
  - Delivery status tracking
- Kafka integration:
  - Event producer with custom serializer
  - Event consumer with custom deserializer
- SIEM exporters:
  - Splunk HEC integration
  - Elasticsearch/ELK export
  - AWS S3 archival

#### SDKs
- **Java SDK**
  - Fluent builder API
  - Synchronous and asynchronous methods
  - Automatic retry with backoff
  - Connection pooling
- **JavaScript/TypeScript SDK**
  - Promise-based async API
  - TypeScript type definitions
  - Browser and Node.js support
- **Python SDK**
  - Sync and async client
  - Type hints
  - Context manager support
- **Go SDK**
  - Context-aware API
  - Functional options pattern
  - Connection pooling

#### Infrastructure
- Docker multi-stage build for optimized images
- Docker Compose for full stack deployment
- Helm chart for Kubernetes deployment
- GitHub Actions CI/CD pipelines:
  - CI on push/PR
  - Docker image release
  - SDK releases to registries
  - Security scanning with CodeQL
- Dependabot configuration

### Security
- API key authentication
- Hash chain integrity verification
- ECDSA signature verification
- Non-root Docker container
- Input validation on all endpoints
- Rate limiting support

### Documentation
- Comprehensive README
- API documentation with examples
- SDK guides for all languages
- Deployment guides (Docker, Kubernetes)
- OpenAPI 3.0 specification

---

## Version History

| Version | Date | Highlights |
|---------|------|------------|
| 1.0.0 | 2025-01 | Initial release with full feature set |

---

## Migration Guides

### Upgrading to 1.0.0

This is the initial release. No migration required.

---

## Links

[Unreleased]: https://github.com/Mohmk10/audit-trail-server/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/Mohmk10/audit-trail-server/releases/tag/v1.0.0
