import { describe, it, expect } from 'vitest';
import {
  AuditTrailClient,
  ActorBuilder,
  ActionBuilder,
  ResourceBuilder,
  MetadataBuilder,
  EventBuilder
} from '../src';

// Ces tests nécessitent le serveur en cours d'exécution
// Pour exécuter: npm run test:integration
describe.skip('Integration Tests', () => {
  const client = AuditTrailClient.builder()
    .serverUrl('http://localhost:8080')
    .build();

  it('should log event successfully', async () => {
    const event = new EventBuilder()
      .actor(ActorBuilder.user('sdk-js-test', 'JS SDK Tester'))
      .action(ActionBuilder.create('SDK JS integration test'))
      .resource(ResourceBuilder.document('doc-js-001', 'Test Document'))
      .metadata(new MetadataBuilder()
        .source('sdk-js-test')
        .tenantId('tenant-001')
        .build())
      .build();

    const response = await client.log(event);

    expect(response.id).toBeDefined();
    expect(response.hash).toBeDefined();
    expect(response.status).toBe('STORED');
  });

  it('should log batch events successfully', async () => {
    const events = [
      new EventBuilder()
        .actor(ActorBuilder.user('batch-user-1', 'User 1'))
        .action(ActionBuilder.create('Batch event 1'))
        .resource(ResourceBuilder.document('batch-doc-1', 'Doc 1'))
        .metadata(new MetadataBuilder().source('batch-test').tenantId('tenant-001').build())
        .build(),
      new EventBuilder()
        .actor(ActorBuilder.user('batch-user-2', 'User 2'))
        .action(ActionBuilder.update('Batch event 2'))
        .resource(ResourceBuilder.document('batch-doc-2', 'Doc 2'))
        .metadata(new MetadataBuilder().source('batch-test').tenantId('tenant-001').build())
        .build(),
    ];

    const response = await client.logBatch(events);

    expect(response.total).toBe(2);
    expect(response.succeeded).toBe(2);
    expect(response.failed).toBe(0);
  });

  it('should get event by id', async () => {
    // First create an event
    const event = new EventBuilder()
      .actor(ActorBuilder.user('test-user', 'Test'))
      .action(ActionBuilder.read('Read test'))
      .resource(ResourceBuilder.document('doc-1', 'Doc'))
      .metadata(new MetadataBuilder().source('test').tenantId('tenant-001').build())
      .build();

    const created = await client.log(event);
    const retrieved = await client.getById(created.id);

    expect(retrieved).not.toBeNull();
    expect(retrieved?.id).toBe(created.id);
  });

  it('should return null for non-existent event', async () => {
    const result = await client.getById('non-existent-id-12345');
    expect(result).toBeNull();
  });

  it('should search events', async () => {
    // First create an event to search
    const event = new EventBuilder()
      .actor(ActorBuilder.user('search-test-user', 'Search Tester'))
      .action(ActionBuilder.create('Searchable event'))
      .resource(ResourceBuilder.document('search-doc-001', 'Search Doc'))
      .metadata(new MetadataBuilder()
        .source('search-test')
        .tenantId('tenant-001')
        .build())
      .build();

    await client.log(event);

    // Wait a bit for indexing
    await new Promise(resolve => setTimeout(resolve, 1000));

    const result = await client.search({
      tenantId: 'tenant-001',
      actorId: 'search-test-user',
      page: 0,
      size: 10
    });

    expect(result.items).toBeDefined();
    expect(Array.isArray(result.items)).toBe(true);
  });

  it('should quick search events', async () => {
    const result = await client.quickSearch('document', 'tenant-001', 0, 10);

    expect(result.items).toBeDefined();
    expect(Array.isArray(result.items)).toBe(true);
  });
});
