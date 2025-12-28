import { describe, it, expect } from 'vitest';
import {
  AuditTrailClient,
  ActorBuilder,
  ActionBuilder,
  ResourceBuilder,
  MetadataBuilder,
  EventBuilder
} from '../src';

describe('AuditTrailClient', () => {
  it('should build client with config', () => {
    const client = AuditTrailClient.builder()
      .serverUrl('http://localhost:8080')
      .apiKey('test-key')
      .timeout(5000)
      .build();

    expect(client).toBeDefined();
  });

  it('should throw error without serverUrl', () => {
    expect(() => {
      AuditTrailClient.builder().build();
    }).toThrow('serverUrl is required');
  });

  it('should build client with all options', () => {
    const client = AuditTrailClient.builder()
      .serverUrl('http://localhost:8080')
      .apiKey('test-key')
      .timeout(10000)
      .retryAttempts(5)
      .retryDelay(2000)
      .headers({ 'X-Custom-Header': 'value' })
      .build();

    expect(client).toBeDefined();
  });
});

describe('Builders', () => {
  describe('ActorBuilder', () => {
    it('should create actor with factory method - user', () => {
      const actor = ActorBuilder.user('user-123', 'John Doe');
      expect(actor.id).toBe('user-123');
      expect(actor.type).toBe('USER');
      expect(actor.name).toBe('John Doe');
    });

    it('should create actor with factory method - system', () => {
      const actor = ActorBuilder.system('system-001');
      expect(actor.id).toBe('system-001');
      expect(actor.type).toBe('SYSTEM');
    });

    it('should create actor with factory method - service', () => {
      const actor = ActorBuilder.service('svc-001', 'Payment Service');
      expect(actor.id).toBe('svc-001');
      expect(actor.type).toBe('SERVICE');
      expect(actor.name).toBe('Payment Service');
    });

    it('should build actor with builder pattern', () => {
      const actor = new ActorBuilder()
        .id('user-456')
        .type('USER')
        .name('Jane Doe')
        .ip('192.168.1.1')
        .userAgent('Mozilla/5.0')
        .attributes({ department: 'IT' })
        .build();

      expect(actor.id).toBe('user-456');
      expect(actor.type).toBe('USER');
      expect(actor.name).toBe('Jane Doe');
      expect(actor.ip).toBe('192.168.1.1');
      expect(actor.userAgent).toBe('Mozilla/5.0');
      expect(actor.attributes?.department).toBe('IT');
    });

    it('should throw error when id is missing', () => {
      expect(() => {
        new ActorBuilder().type('USER').build();
      }).toThrow('Actor id and type are required');
    });

    it('should throw error when type is missing', () => {
      expect(() => {
        new ActorBuilder().id('user-123').build();
      }).toThrow('Actor id and type are required');
    });
  });

  describe('ActionBuilder', () => {
    it('should create action with factory method - create', () => {
      const action = ActionBuilder.create('Created document');
      expect(action.type).toBe('CREATE');
      expect(action.description).toBe('Created document');
    });

    it('should create action with factory method - read', () => {
      const action = ActionBuilder.read();
      expect(action.type).toBe('READ');
    });

    it('should create action with factory method - update', () => {
      const action = ActionBuilder.update('Updated settings');
      expect(action.type).toBe('UPDATE');
      expect(action.description).toBe('Updated settings');
    });

    it('should create action with factory method - delete', () => {
      const action = ActionBuilder.delete();
      expect(action.type).toBe('DELETE');
    });

    it('should create action with factory method - login', () => {
      const action = ActionBuilder.login();
      expect(action.type).toBe('LOGIN');
      expect(action.description).toBe('User login');
    });

    it('should create action with factory method - logout', () => {
      const action = ActionBuilder.logout();
      expect(action.type).toBe('LOGOUT');
      expect(action.description).toBe('User logout');
    });

    it('should build action with builder pattern', () => {
      const action = new ActionBuilder()
        .type('CUSTOM_ACTION')
        .description('Custom action description')
        .category('SECURITY')
        .build();

      expect(action.type).toBe('CUSTOM_ACTION');
      expect(action.description).toBe('Custom action description');
      expect(action.category).toBe('SECURITY');
    });

    it('should throw error when type is missing', () => {
      expect(() => {
        new ActionBuilder().description('test').build();
      }).toThrow('Action type is required');
    });
  });

  describe('ResourceBuilder', () => {
    it('should create resource with factory method - of', () => {
      const resource = ResourceBuilder.of('res-001', 'CONFIG', 'App Config');
      expect(resource.id).toBe('res-001');
      expect(resource.type).toBe('CONFIG');
      expect(resource.name).toBe('App Config');
    });

    it('should create resource with factory method - document', () => {
      const resource = ResourceBuilder.document('doc-456', 'Report');
      expect(resource.id).toBe('doc-456');
      expect(resource.type).toBe('DOCUMENT');
      expect(resource.name).toBe('Report');
    });

    it('should create resource with factory method - user', () => {
      const resource = ResourceBuilder.user('user-789', 'John');
      expect(resource.id).toBe('user-789');
      expect(resource.type).toBe('USER');
    });

    it('should create resource with factory method - transaction', () => {
      const resource = ResourceBuilder.transaction('txn-001', 'Payment');
      expect(resource.id).toBe('txn-001');
      expect(resource.type).toBe('TRANSACTION');
    });

    it('should build resource with before/after', () => {
      const resource = new ResourceBuilder()
        .id('user-001')
        .type('USER')
        .name('User Profile')
        .before({ status: 'active' })
        .after({ status: 'inactive' })
        .build();

      expect(resource.before).toEqual({ status: 'active' });
      expect(resource.after).toEqual({ status: 'inactive' });
    });

    it('should throw error when id is missing', () => {
      expect(() => {
        new ResourceBuilder().type('DOCUMENT').build();
      }).toThrow('Resource id and type are required');
    });
  });

  describe('MetadataBuilder', () => {
    it('should build metadata with required fields', () => {
      const metadata = new MetadataBuilder()
        .source('web-app')
        .tenantId('tenant-001')
        .build();

      expect(metadata.source).toBe('web-app');
      expect(metadata.tenantId).toBe('tenant-001');
    });

    it('should build metadata with all fields', () => {
      const metadata = new MetadataBuilder()
        .source('web-app')
        .tenantId('tenant-001')
        .correlationId('corr-123')
        .sessionId('sess-456')
        .tags({ env: 'production' })
        .extra({ custom: 'data' })
        .build();

      expect(metadata.correlationId).toBe('corr-123');
      expect(metadata.sessionId).toBe('sess-456');
      expect(metadata.tags?.env).toBe('production');
      expect(metadata.extra?.custom).toBe('data');
    });

    it('should throw error when source is missing', () => {
      expect(() => {
        new MetadataBuilder().tenantId('tenant-001').build();
      }).toThrow('Source and tenantId are required');
    });

    it('should throw error when tenantId is missing', () => {
      expect(() => {
        new MetadataBuilder().source('web-app').build();
      }).toThrow('Source and tenantId are required');
    });
  });

  describe('EventBuilder', () => {
    it('should build complete event', () => {
      const event = new EventBuilder()
        .actor(ActorBuilder.user('user-123', 'John'))
        .action(ActionBuilder.create('Created'))
        .resource(ResourceBuilder.document('doc-1', 'Report'))
        .metadata(new MetadataBuilder()
          .source('web-app')
          .tenantId('tenant-001')
          .build())
        .build();

      expect(event.actor.id).toBe('user-123');
      expect(event.action.type).toBe('CREATE');
      expect(event.resource.type).toBe('DOCUMENT');
      expect(event.metadata.tenantId).toBe('tenant-001');
    });

    it('should throw error when actor is missing', () => {
      expect(() => {
        new EventBuilder()
          .action(ActionBuilder.create())
          .resource(ResourceBuilder.document('doc-1'))
          .metadata(new MetadataBuilder().source('app').tenantId('t1').build())
          .build();
      }).toThrow('Actor, action, resource, and metadata are required');
    });

    it('should throw error when action is missing', () => {
      expect(() => {
        new EventBuilder()
          .actor(ActorBuilder.user('u1'))
          .resource(ResourceBuilder.document('doc-1'))
          .metadata(new MetadataBuilder().source('app').tenantId('t1').build())
          .build();
      }).toThrow('Actor, action, resource, and metadata are required');
    });

    it('should throw error when resource is missing', () => {
      expect(() => {
        new EventBuilder()
          .actor(ActorBuilder.user('u1'))
          .action(ActionBuilder.create())
          .metadata(new MetadataBuilder().source('app').tenantId('t1').build())
          .build();
      }).toThrow('Actor, action, resource, and metadata are required');
    });

    it('should throw error when metadata is missing', () => {
      expect(() => {
        new EventBuilder()
          .actor(ActorBuilder.user('u1'))
          .action(ActionBuilder.create())
          .resource(ResourceBuilder.document('doc-1'))
          .build();
      }).toThrow('Actor, action, resource, and metadata are required');
    });
  });
});
