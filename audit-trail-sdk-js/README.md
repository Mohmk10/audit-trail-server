# Audit Trail SDK for JavaScript/TypeScript

Official JavaScript/TypeScript SDK for Audit Trail Universal.

## Installation

```bash
npm install @mohmk10/audit-trail-sdk
```

## Quick Start

```typescript
import {
  AuditTrailClient,
  ActorBuilder,
  ActionBuilder,
  ResourceBuilder,
  MetadataBuilder,
  EventBuilder
} from '@mohmk10/audit-trail-sdk';

// Create client
const client = AuditTrailClient.builder()
  .serverUrl('https://audit.example.com')
  .apiKey('your-api-key')
  .build();

// Log an event
const response = await client.log(
  new EventBuilder()
    .actor(ActorBuilder.user('user-123', 'John Doe'))
    .action(ActionBuilder.create('Created document'))
    .resource(ResourceBuilder.document('doc-456', 'Q4 Report'))
    .metadata(new MetadataBuilder()
      .source('web-app')
      .tenantId('tenant-001')
      .build())
    .build()
);

console.log('Event logged:', response.id);
```

## Features

- TypeScript support with full type definitions
- Browser and Node.js compatible
- Fluent builder API
- Automatic retry with exponential backoff
- Batch event logging
- Search capabilities

## API Reference

### Client Configuration

```typescript
const client = AuditTrailClient.builder()
  .serverUrl('http://localhost:8080')  // Required
  .apiKey('your-api-key')              // Optional
  .timeout(30000)                      // Default: 30000ms
  .retryAttempts(3)                    // Default: 3
  .retryDelay(1000)                    // Default: 1000ms
  .headers({ 'X-Custom': 'value' })    // Optional custom headers
  .build();
```

### Logging Events

#### Single Event

```typescript
const response = await client.log(event);
// Response: { id, timestamp, hash, status }
```

#### Batch Events

```typescript
const response = await client.logBatch([event1, event2, event3]);
// Response: { total, succeeded, failed, events, errors? }
```

### Building Events

#### Actor Types

```typescript
// Factory methods
const user = ActorBuilder.user('user-123', 'John Doe');
const system = ActorBuilder.system('cron-job');
const service = ActorBuilder.service('payment-svc', 'Payment Service');

// Builder pattern
const actor = new ActorBuilder()
  .id('user-123')
  .type('USER')
  .name('John Doe')
  .ip('192.168.1.1')
  .userAgent('Mozilla/5.0...')
  .attributes({ department: 'IT' })
  .build();
```

#### Action Types

```typescript
// Factory methods
const create = ActionBuilder.create('Created resource');
const read = ActionBuilder.read('Viewed resource');
const update = ActionBuilder.update('Modified resource');
const del = ActionBuilder.delete('Removed resource');
const login = ActionBuilder.login();
const logout = ActionBuilder.logout();

// Builder pattern
const action = new ActionBuilder()
  .type('CUSTOM_ACTION')
  .description('Custom action description')
  .category('SECURITY')
  .build();
```

#### Resource Types

```typescript
// Factory methods
const doc = ResourceBuilder.document('doc-123', 'Report');
const user = ResourceBuilder.user('user-456', 'John');
const txn = ResourceBuilder.transaction('txn-789', 'Payment');
const custom = ResourceBuilder.of('id', 'CUSTOM_TYPE', 'Name');

// With before/after state
const resource = new ResourceBuilder()
  .id('user-123')
  .type('USER')
  .name('User Profile')
  .before({ status: 'active' })
  .after({ status: 'inactive' })
  .build();
```

#### Metadata

```typescript
const metadata = new MetadataBuilder()
  .source('web-app')           // Required
  .tenantId('tenant-001')      // Required
  .correlationId('corr-123')   // Optional
  .sessionId('sess-456')       // Optional
  .tags({ env: 'production' }) // Optional
  .extra({ custom: 'data' })   // Optional
  .build();
```

### Searching Events

#### Advanced Search

```typescript
const result = await client.search({
  tenantId: 'tenant-001',      // Required
  actorId: 'user-123',         // Optional
  actorType: 'USER',           // Optional
  actionType: 'CREATE',        // Optional
  resourceId: 'doc-456',       // Optional
  resourceType: 'DOCUMENT',    // Optional
  fromDate: '2024-01-01',      // Optional
  toDate: '2024-12-31',        // Optional
  query: 'search term',        // Optional
  page: 0,                     // Optional, default: 0
  size: 20                     // Optional, default: 20
});

// Result: { items, totalCount, page, size, totalPages }
```

#### Quick Search

```typescript
const result = await client.quickSearch('search term', 'tenant-001', 0, 20);
```

### Retrieving Events

```typescript
const event = await client.getById('event-id');
// Returns null if not found
```

## Error Handling

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
    console.error('API error:', error.statusCode, error.body);
  } else if (error instanceof AuditTrailValidationError) {
    console.error('Validation errors:', error.violations);
  }
}
```

## Browser Usage

The SDK works in modern browsers that support the Fetch API:

```html
<script type="module">
  import { AuditTrailClient, ActorBuilder, ActionBuilder, ResourceBuilder, MetadataBuilder, EventBuilder }
    from 'https://unpkg.com/@mohmk10/audit-trail-sdk/dist/index.mjs';

  const client = AuditTrailClient.builder()
    .serverUrl('https://audit.example.com')
    .build();
</script>
```

## License

MIT
