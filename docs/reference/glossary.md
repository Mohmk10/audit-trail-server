# Glossary

Definitions des termes utilises dans Audit Trail.

## A

### Actor
L'entite qui effectue une action. Peut etre un utilisateur humain (`USER`), un processus systeme (`SYSTEM`), ou un service (`SERVICE`).

### Action
L'operation effectuee par un acteur sur une ressource. Types standards : CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT.

### Agregation
Calcul statistique sur un ensemble d'evenements. Exemple : nombre d'evenements par type d'action.

### Alert
Notification generee lorsqu'une regle de detection est declenchee.

### API Key
Cle d'authentification pour acceder a l'API. Format : `atk_live_...` ou `atk_test_...`.

### Audit Event
Enregistrement immutable d'une action effectuee dans le systeme.

### Audit Log
Journal chronologique des evenements d'audit.

### Audit Trail
Chaine complete d'evenements permettant de retracer l'historique des actions.

## B

### Batch
Envoi de plusieurs evenements en une seule requete (max 1000).

### Before/After
Etats d'une ressource avant et apres une modification, permettant de retracer les changements.

## C

### Category
Classification d'une action. Exemples : DOCUMENT, AUTHENTICATION, TRANSACTION.

### Chain Hashing
Technique ou chaque evenement inclut le hash de l'evenement precedent, garantissant l'integrite de la sequence.

### Compliance
Conformite aux regulations (GDPR, SOX, HIPAA, etc.).

### Correlation ID
Identifiant unique permettant de relier des evenements lies entre differents systemes.

## D

### Dead Letter Queue (DLQ)
File d'attente pour les messages qui n'ont pas pu etre traites.

### Detection Rule
Regle definissant les conditions de declenchement d'une alerte.

## E

### Elasticsearch
Moteur de recherche utilise pour l'indexation et la recherche full-text des evenements.

### Event
Synonyme d'Audit Event. Enregistrement d'une action.

### Event Sourcing
Pattern architectural ou l'etat est derive de la sequence d'evenements.

## F

### Fire-and-Forget
Mode d'envoi ou le client n'attend pas la confirmation du serveur.

### Full-Text Search
Recherche dans le contenu textuel des evenements (descriptions, noms, etc.).

## H

### Hash
Empreinte cryptographique (SHA-256) d'un evenement, garantissant son integrite.

### Hexagonal Architecture
Architecture logicielle separant le domaine metier des adaptateurs techniques.

## I

### Immutability
Caracteristique des evenements : une fois crees, ils ne peuvent pas etre modifies.

### Ingestion
Processus de reception et de stockage des evenements.

### Integrity
Garantie que les donnees n'ont pas ete alterees.

## K

### Kafka
Plateforme de streaming distribuee utilisee pour l'ingestion et l'export d'evenements.

## M

### Metadata
Donnees contextuelles associees a un evenement (source, tenantId, correlationId, etc.).

### Multi-tenancy
Architecture permettant d'isoler les donnees de differents clients (tenants).

## N

### Non-repudiation
Garantie qu'un acteur ne peut pas nier avoir effectue une action.

## P

### Pagination
Technique de division des resultats en pages pour les grandes listes.

### Port
Dans l'architecture hexagonale, interface definissant les interactions avec l'exterieur.

### PostgreSQL
Base de donnees relationnelle utilisee pour le stockage primaire des evenements.

## Q

### Query
Requete de recherche ou d'interrogation des evenements.

## R

### Rate Limiting
Limitation du nombre de requetes par periode de temps.

### RBAC (Role-Based Access Control)
Controle d'acces base sur les roles (ADMIN, MANAGER, OPERATOR, VIEWER).

### Redis
Cache distribue utilise pour les performances et les sessions.

### Report
Document genere a partir des evenements (PDF, CSV, Excel, JSON).

### Resource
Objet sur lequel une action est effectuee (document, utilisateur, transaction, etc.).

### Retention
Duree de conservation des evenements avant archivage ou suppression.

### Rule
Voir Detection Rule.

## S

### SDK (Software Development Kit)
Bibliotheque facilitant l'integration avec Audit Trail (Java, JavaScript, Python, Go).

### Session ID
Identifiant de session utilisateur.

### Severity
Niveau de gravite d'une alerte : LOW, MEDIUM, HIGH, CRITICAL.

### SIEM (Security Information and Event Management)
Systeme de gestion des informations et evenements de securite.

### Signature
Signature numerique optionnelle d'un evenement pour la non-repudiation.

### Source
Origine d'un evenement (application, service, systeme).

## T

### Tenant
Organisation ou client dans un systeme multi-tenant. Les donnees sont isolees par tenant.

### Timeline
Vue chronologique des evenements groupes par periode.

### Timestamp
Horodatage precis d'un evenement (ISO 8601).

### Tracing
Suivi distribue des requetes a travers les systemes.

## U

### User Agent
Identifiant du client (navigateur, SDK, etc.).

### UUID
Identifiant unique universel (format: 550e8400-e29b-41d4-a716-446655440000).

## V

### Validation
Verification de la conformite des donnees avant ingestion.

### Value Object
Objet immutable defini par ses attributs (Actor, Action, Resource).

### Violation
Erreur de validation specifique (champ manquant, format invalide, etc.).

## W

### Webhook
Mecanisme de notification HTTP pour les evenements en temps reel.

### Wildcard
Caractere joker pour la recherche (* pour plusieurs caracteres, ? pour un seul).

## Acronymes

| Acronyme | Signification |
|----------|---------------|
| API | Application Programming Interface |
| CQRS | Command Query Responsibility Segregation |
| DDD | Domain-Driven Design |
| DLQ | Dead Letter Queue |
| GDPR | General Data Protection Regulation |
| HIPAA | Health Insurance Portability and Accountability Act |
| HPA | Horizontal Pod Autoscaler |
| HTTP | Hypertext Transfer Protocol |
| HTTPS | HTTP Secure |
| JSON | JavaScript Object Notation |
| JWT | JSON Web Token |
| RBAC | Role-Based Access Control |
| REST | Representational State Transfer |
| SDK | Software Development Kit |
| SIEM | Security Information and Event Management |
| SOX | Sarbanes-Oxley Act |
| SQL | Structured Query Language |
| SSL | Secure Sockets Layer |
| TLS | Transport Layer Security |
| UUID | Universally Unique Identifier |
