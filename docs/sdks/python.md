# SDK Python

Guide complet du SDK Python pour Audit Trail.

## Installation

```bash
pip install audit-trail-sdk
```

Ou avec Poetry :

```bash
poetry add audit-trail-sdk
```

## Configuration

### Configuration basique

```python
from audit_trail import AuditTrailClient, Actor, Action, Resource

client = AuditTrailClient.builder() \
    .server_url("https://audit.example.com") \
    .api_key("your-api-key") \
    .build()
```

### Configuration avancee

```python
client = AuditTrailClient.builder() \
    .server_url("https://audit.example.com") \
    .api_key("your-api-key") \
    .timeout(30) \
    .retry_attempts(3) \
    .retry_delay(0.5) \
    .build()
```

## Logging d'evenements

### Synchrone

```python
from audit_trail import Event, Actor, Action, Resource, EventMetadata

response = client.log(Event.create(
    actor=Actor.user("user-123", "John Doe"),
    action=Action.create("Created document"),
    resource=Resource.document("doc-456", "Q4 Report"),
    metadata=EventMetadata(
        source="web-app",
        tenant_id="tenant-001",
        correlation_id="req-789"
    )
))

print(f"Event ID: {response.id}")
print(f"Hash: {response.hash}")
```

### Asynchrone (asyncio)

```python
import asyncio
from audit_trail import AsyncAuditTrailClient

async def main():
    client = AsyncAuditTrailClient.builder() \
        .server_url("https://audit.example.com") \
        .api_key("your-api-key") \
        .build()

    response = await client.log(Event.create(
        actor=Actor.user("user-123", "John Doe"),
        action=Action.create("Created document"),
        resource=Resource.document("doc-456", "Q4 Report")
    ))

    print(f"Event logged: {response.id}")

asyncio.run(main())
```

### Fire-and-forget

```python
# Non-bloquant, ne pas attendre la reponse
client.log_async(Event.create(
    actor=Actor.user("user-123", "John Doe"),
    action=Action.read("Viewed document"),
    resource=Resource.document("doc-456", "Q4 Report")
))
```

### Batch

```python
events = [event1, event2, event3]
response = client.log_batch(events)

print(f"Total: {response.total}")
print(f"Succeeded: {response.succeeded}")
print(f"Failed: {response.failed}")

for error in response.errors:
    print(f"Error at index {error.index}: {error.message}")
```

## Builders

### Actor

```python
from audit_trail import Actor, ActorType

# Utilisateur
user = Actor.user("user-123", "John Doe")

# Systeme
system = Actor.system("batch-processor")

# Service
service = Actor.service("api-gateway", "API Gateway")

# Avec attributs
user_with_attrs = Actor.builder() \
    .id("user-123") \
    .type(ActorType.USER) \
    .name("John Doe") \
    .ip("192.168.1.100") \
    .user_agent("Mozilla/5.0...") \
    .attribute("department", "Engineering") \
    .attribute("role", "Developer") \
    .build()
```

### Action

```python
from audit_trail import Action

# Actions predefinies
create = Action.create("Created document")
read = Action.read("Viewed document")
update = Action.update("Updated document")
delete = Action.delete("Deleted document")
login = Action.login()
logout = Action.logout()

# Action personnalisee
custom = Action.of("APPROVE", "Approved request", "WORKFLOW")
```

### Resource

```python
from audit_trail import Resource, ResourceType

# Types predefinis
doc = Resource.document("doc-456", "Q4 Report")
user = Resource.user("user-789", "Jane Smith")
txn = Resource.transaction("txn-123", "Payment #456")

# Avec changements
with_changes = Resource.builder() \
    .id("doc-456") \
    .type(ResourceType.DOCUMENT) \
    .name("Q4 Report") \
    .before({"status": "draft", "version": 1}) \
    .after({"status": "published", "version": 2}) \
    .build()
```

### Metadata

```python
from audit_trail import EventMetadata

metadata = EventMetadata.builder() \
    .source("web-app") \
    .tenant_id("tenant-001") \
    .correlation_id("req-789") \
    .session_id("sess-abc") \
    .tag("priority", "high") \
    .tag("environment", "production") \
    .build()
```

## Gestion des erreurs

```python
from audit_trail.exceptions import (
    AuditTrailError,
    AuditTrailConnectionError,
    AuditTrailApiError,
    AuditTrailValidationError
)

try:
    client.log(event)
except AuditTrailConnectionError as e:
    # Erreur de connexion (reseau, timeout)
    print(f"Connection failed: {e}")
    # Retry ou fallback
except AuditTrailApiError as e:
    # Erreur API (4xx, 5xx)
    print(f"API error {e.status_code}: {e.message}")
    if e.status_code == 429:
        # Rate limited - attendre et reessayer
        pass
except AuditTrailValidationError as e:
    # Erreur de validation
    print(f"Validation failed: {e.violations}")
    # Corriger les donnees
```

## Integration Django

### Configuration

```python
# settings.py
AUDIT_TRAIL = {
    'SERVER_URL': os.environ.get('AUDIT_TRAIL_URL'),
    'API_KEY': os.environ.get('AUDIT_TRAIL_API_KEY'),
    'TIMEOUT': 30,
    'RETRY_ATTEMPTS': 3,
}
```

### Client singleton

```python
# audit/client.py
from django.conf import settings
from audit_trail import AuditTrailClient

_client = None

def get_audit_client():
    global _client
    if _client is None:
        config = settings.AUDIT_TRAIL
        _client = AuditTrailClient.builder() \
            .server_url(config['SERVER_URL']) \
            .api_key(config['API_KEY']) \
            .timeout(config.get('TIMEOUT', 30)) \
            .retry_attempts(config.get('RETRY_ATTEMPTS', 3)) \
            .build()
    return _client
```

### Middleware

```python
# audit/middleware.py
from audit_trail import Actor, Action, Resource, Event
from .client import get_audit_client

class AuditMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response
        self.client = get_audit_client()

    def __call__(self, request):
        response = self.get_response(request)

        if request.user.is_authenticated:
            self.client.log_async(Event.create(
                actor=Actor.user(str(request.user.id), request.user.username),
                action=Action.of(request.method, f"{request.method} {request.path}", "API"),
                resource=Resource.of(request.path, "ENDPOINT", request.path),
                metadata={
                    "source": "django-app",
                    "tenant_id": getattr(request, 'tenant_id', None),
                    "correlation_id": request.headers.get('X-Correlation-ID')
                }
            ))

        return response
```

### Decorateur

```python
# audit/decorators.py
from functools import wraps
from audit_trail import Actor, Action, Resource, Event
from .client import get_audit_client

def audited(action_type, description=None, category=None):
    def decorator(view_func):
        @wraps(view_func)
        def wrapper(request, *args, **kwargs):
            result = view_func(request, *args, **kwargs)

            client = get_audit_client()
            client.log_async(Event.create(
                actor=Actor.user(str(request.user.id), request.user.username),
                action=Action.of(action_type, description or action_type, category),
                resource=Resource.of(
                    kwargs.get('pk', 'unknown'),
                    category or 'RESOURCE',
                    request.path
                )
            ))

            return result
        return wrapper
    return decorator

# Utilisation
@audited("READ", "Viewed document", "DOCUMENT")
def document_detail(request, pk):
    # ...
```

## Integration FastAPI

### Configuration

```python
# app/audit.py
from contextlib import asynccontextmanager
from audit_trail import AsyncAuditTrailClient
from app.config import settings

audit_client: AsyncAuditTrailClient = None

@asynccontextmanager
async def lifespan(app):
    global audit_client
    audit_client = AsyncAuditTrailClient.builder() \
        .server_url(settings.audit_trail_url) \
        .api_key(settings.audit_trail_api_key) \
        .build()
    yield
    await audit_client.close()

def get_audit_client():
    return audit_client
```

### Middleware

```python
# app/middleware.py
from starlette.middleware.base import BaseHTTPMiddleware
from audit_trail import Actor, Action, Resource, Event
from app.audit import get_audit_client

class AuditMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        response = await call_next(request)

        if hasattr(request.state, 'user'):
            user = request.state.user
            client = get_audit_client()
            await client.log(Event.create(
                actor=Actor.user(user.id, user.name),
                action=Action.of(request.method, f"{request.method} {request.url.path}", "API"),
                resource=Resource.of(request.url.path, "ENDPOINT", request.url.path)
            ))

        return response
```

### Dependance

```python
# app/dependencies.py
from fastapi import Depends
from audit_trail import AsyncAuditTrailClient
from app.audit import get_audit_client

async def audit_log(
    client: AsyncAuditTrailClient = Depends(get_audit_client)
):
    async def log_event(actor, action, resource, metadata=None):
        await client.log(Event.create(
            actor=actor,
            action=action,
            resource=resource,
            metadata=metadata
        ))
    return log_event

# Utilisation
@app.get("/documents/{id}")
async def get_document(
    id: str,
    current_user: User = Depends(get_current_user),
    audit: Callable = Depends(audit_log)
):
    document = await document_service.get(id)

    await audit(
        Actor.user(current_user.id, current_user.name),
        Action.read("Viewed document"),
        Resource.document(id, document.name)
    )

    return document
```

## Integration Flask

```python
from flask import Flask, g, request
from audit_trail import AuditTrailClient, Actor, Action, Resource, Event

app = Flask(__name__)

# Configuration
app.config['AUDIT_TRAIL_URL'] = os.environ.get('AUDIT_TRAIL_URL')
app.config['AUDIT_TRAIL_API_KEY'] = os.environ.get('AUDIT_TRAIL_API_KEY')

# Client
audit_client = AuditTrailClient.builder() \
    .server_url(app.config['AUDIT_TRAIL_URL']) \
    .api_key(app.config['AUDIT_TRAIL_API_KEY']) \
    .build()

@app.after_request
def audit_request(response):
    if hasattr(g, 'user'):
        audit_client.log_async(Event.create(
            actor=Actor.user(g.user.id, g.user.name),
            action=Action.of(request.method, f"{request.method} {request.path}", "API"),
            resource=Resource.of(request.path, "ENDPOINT", request.path),
            metadata={
                "source": "flask-app",
                "status_code": response.status_code
            }
        ))
    return response
```

## Types Python (Type Hints)

```python
from audit_trail.types import (
    Event,
    EventResponse,
    BatchEventResponse,
    Actor,
    Action,
    Resource,
    EventMetadata
)

def log_document_action(
    user_id: str,
    user_name: str,
    action_type: str,
    document_id: str,
    document_name: str
) -> EventResponse:
    event: Event = Event.create(
        actor=Actor.user(user_id, user_name),
        action=Action.of(action_type, f"{action_type} document"),
        resource=Resource.document(document_id, document_name)
    )
    return client.log(event)
```

## Context Manager

```python
from audit_trail import AuditTrailClient

# Auto-close
with AuditTrailClient.builder() \
    .server_url("https://audit.example.com") \
    .api_key("your-api-key") \
    .build() as client:

    client.log(event)
# Client automatiquement ferme
```

## Async Context Manager

```python
from audit_trail import AsyncAuditTrailClient

async with AsyncAuditTrailClient.builder() \
    .server_url("https://audit.example.com") \
    .api_key("your-api-key") \
    .build() as client:

    await client.log(event)
# Client automatiquement ferme
```

## Bonnes pratiques

1. **Utilisez l'async** avec FastAPI/asyncio
2. **Utilisez le context manager** pour la gestion des ressources
3. **Type hints** pour une meilleure maintenabilite
4. **Fire-and-forget** pour les logs non critiques
5. **Gerez les erreurs** avec des fallbacks appropries
