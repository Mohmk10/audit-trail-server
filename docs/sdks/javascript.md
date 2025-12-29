# SDK JavaScript / TypeScript

Guide du SDK JavaScript/TypeScript pour Audit Trail.

## Installation

```bash
npm install @mohmk10/audit-trail-sdk
# ou
yarn add @mohmk10/audit-trail-sdk
```

## Configuration

### ESM (recommande)

```typescript
import { AuditTrailClient, ActorBuilder, ActionBuilder, ResourceBuilder } from '@mohmk10/audit-trail-sdk';

const client = AuditTrailClient.builder()
  .serverUrl('https://audit.example.com')
  .apiKey('your-api-key')
  .build();
```

### CommonJS

```javascript
const { AuditTrailClient, ActorBuilder, ActionBuilder, ResourceBuilder } = require('@mohmk10/audit-trail-sdk');
```

### Configuration avancee

```typescript
const client = AuditTrailClient.builder()
  .serverUrl('https://audit.example.com')
  .apiKey('your-api-key')
  .timeout(30000) // 30 secondes
  .retryAttempts(3)
  .retryDelay(500) // 500ms
  .build();
```

## Logging d'evenements

### Async/Await

```typescript
const response = await client.log({
  actor: ActorBuilder.user('user-123', 'John Doe'),
  action: ActionBuilder.create('Created document'),
  resource: ResourceBuilder.document('doc-456', 'Q4 Report'),
  metadata: {
    source: 'web-app',
    tenantId: 'tenant-001'
  }
});

console.log('Event ID:', response.id);
console.log('Hash:', response.hash);
```

### Promise

```typescript
client.log({
  actor: ActorBuilder.user('user-123', 'John Doe'),
  action: ActionBuilder.create('Created document'),
  resource: ResourceBuilder.document('doc-456', 'Q4 Report')
})
.then(response => {
  console.log('Event logged:', response.id);
})
.catch(error => {
  console.error('Failed:', error.message);
});
```

### Fire-and-forget

```typescript
// Ne pas attendre la reponse
client.logAsync({
  actor: ActorBuilder.user('user-123', 'John Doe'),
  action: ActionBuilder.read('Viewed document'),
  resource: ResourceBuilder.document('doc-456', 'Q4 Report')
});
```

### Batch

```typescript
const events = [event1, event2, event3];
const response = await client.logBatch(events);

console.log('Total:', response.total);
console.log('Succeeded:', response.succeeded);
console.log('Failed:', response.failed);

response.errors?.forEach(error => {
  console.error(`Error at index ${error.index}: ${error.message}`);
});
```

## Builders

### ActorBuilder

```typescript
// Utilisateur
const user = ActorBuilder.user('user-123', 'John Doe');

// Systeme
const system = ActorBuilder.system('batch-processor');

// Service
const service = ActorBuilder.service('api-gateway', 'API Gateway');

// Avec options
const userWithDetails = ActorBuilder.user('user-123', 'John Doe')
  .withIp('192.168.1.100')
  .withUserAgent('Mozilla/5.0...')
  .withAttribute('department', 'Engineering')
  .build();
```

### ActionBuilder

```typescript
// Actions predefinies
const create = ActionBuilder.create('Created document');
const read = ActionBuilder.read('Viewed document');
const update = ActionBuilder.update('Updated document');
const del = ActionBuilder.delete('Deleted document');
const login = ActionBuilder.login();
const logout = ActionBuilder.logout();

// Action personnalisee
const custom = ActionBuilder.of('APPROVE', 'Approved request', 'WORKFLOW');
```

### ResourceBuilder

```typescript
// Types predefinis
const doc = ResourceBuilder.document('doc-456', 'Q4 Report');
const user = ResourceBuilder.user('user-789', 'Jane Smith');
const txn = ResourceBuilder.transaction('txn-123', 'Payment #456');

// Avec changements
const withChanges = ResourceBuilder.document('doc-456', 'Q4 Report')
  .withBefore({ status: 'draft', version: 1 })
  .withAfter({ status: 'published', version: 2 })
  .build();
```

## Gestion des erreurs

```typescript
import {
  AuditTrailError,
  AuditTrailConnectionError,
  AuditTrailApiError,
  AuditTrailValidationError
} from '@mohmk10/audit-trail-sdk';

try {
  await client.log(event);
} catch (error) {
  if (error instanceof AuditTrailConnectionError) {
    console.error('Connection failed:', error.message);
  } else if (error instanceof AuditTrailApiError) {
    console.error(`API error ${error.statusCode}:`, error.message);
    if (error.statusCode === 429) {
      // Rate limited
    }
  } else if (error instanceof AuditTrailValidationError) {
    console.error('Validation failed:', error.violations);
  }
}
```

## Integration Express.js

### Middleware

```typescript
import express from 'express';
import { AuditTrailClient, ActorBuilder, ActionBuilder, ResourceBuilder } from '@mohmk10/audit-trail-sdk';

const app = express();
const auditClient = AuditTrailClient.builder()
  .serverUrl(process.env.AUDIT_TRAIL_URL!)
  .apiKey(process.env.AUDIT_TRAIL_API_KEY!)
  .build();

// Middleware d'audit
const auditMiddleware = (action: string) => {
  return (req: express.Request, res: express.Response, next: express.NextFunction) => {
    const originalSend = res.send;

    res.send = function(body) {
      auditClient.logAsync({
        actor: ActorBuilder.user(req.user?.id || 'anonymous', req.user?.name),
        action: ActionBuilder.of(action, `${req.method} ${req.path}`, 'API'),
        resource: ResourceBuilder.of(req.params.id || 'unknown', 'API_ENDPOINT', req.path),
        metadata: {
          source: 'api-server',
          tenantId: req.headers['x-tenant-id'] as string,
          correlationId: req.headers['x-correlation-id'] as string
        }
      });

      return originalSend.call(this, body);
    };

    next();
  };
};

// Utilisation
app.get('/documents/:id', auditMiddleware('READ'), (req, res) => {
  // ...
});
```

## Integration Next.js

### API Route

```typescript
// pages/api/documents/[id].ts
import type { NextApiRequest, NextApiResponse } from 'next';
import { auditClient } from '@/lib/audit-trail';
import { ActorBuilder, ActionBuilder, ResourceBuilder } from '@mohmk10/audit-trail-sdk';

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { id } = req.query;
  const user = await getUser(req);

  if (req.method === 'GET') {
    const document = await getDocument(id as string);

    await auditClient.log({
      actor: ActorBuilder.user(user.id, user.name),
      action: ActionBuilder.read('Viewed document'),
      resource: ResourceBuilder.document(id as string, document.name),
      metadata: {
        source: 'next-app',
        tenantId: user.tenantId
      }
    });

    return res.json(document);
  }
}
```

## Types TypeScript

```typescript
import type {
  Event,
  EventResponse,
  BatchEventResponse,
  Actor,
  Action,
  Resource,
  EventMetadata
} from '@mohmk10/audit-trail-sdk';

const event: Event = {
  actor: { id: 'user-123', type: 'USER', name: 'John Doe' },
  action: { type: 'CREATE', description: 'Created document' },
  resource: { id: 'doc-456', type: 'DOCUMENT', name: 'Q4 Report' },
  metadata: { source: 'web-app', tenantId: 'tenant-001' }
};
```

## Configuration avec variables d'environnement

```typescript
// lib/audit-trail.ts
import { AuditTrailClient } from '@mohmk10/audit-trail-sdk';

if (!process.env.AUDIT_TRAIL_URL || !process.env.AUDIT_TRAIL_API_KEY) {
  throw new Error('Audit Trail configuration missing');
}

export const auditClient = AuditTrailClient.builder()
  .serverUrl(process.env.AUDIT_TRAIL_URL)
  .apiKey(process.env.AUDIT_TRAIL_API_KEY)
  .timeout(parseInt(process.env.AUDIT_TRAIL_TIMEOUT || '30000'))
  .build();
```

## Bonnes pratiques

1. **Utilisez les builders** pour une meilleure lisibilite
2. **Gerez les erreurs** appropriement
3. **Ne bloquez pas** les requetes utilisateur (fire-and-forget)
4. **Incluez le correlationId** pour le tracing distribue
5. **Typez vos evenements** avec TypeScript
