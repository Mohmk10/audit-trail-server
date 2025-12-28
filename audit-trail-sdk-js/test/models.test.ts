import { describe, it, expect } from 'vitest';
import {
  AuditTrailError,
  AuditTrailConnectionError,
  AuditTrailApiError,
  AuditTrailValidationError,
  validateEvent,
  isValidUUID,
  isValidISODate,
  ActorBuilder,
  ActionBuilder,
  ResourceBuilder,
  MetadataBuilder,
  EventBuilder
} from '../src';

describe('Errors', () => {
  describe('AuditTrailError', () => {
    it('should create base error', () => {
      const error = new AuditTrailError('Test error');
      expect(error.message).toBe('Test error');
      expect(error.name).toBe('AuditTrailError');
      expect(error instanceof Error).toBe(true);
    });
  });

  describe('AuditTrailConnectionError', () => {
    it('should create connection error', () => {
      const cause = new Error('Network failure');
      const error = new AuditTrailConnectionError('Failed to connect', cause);
      expect(error.message).toBe('Failed to connect');
      expect(error.name).toBe('AuditTrailConnectionError');
      expect(error.cause).toBe(cause);
    });

    it('should work without cause', () => {
      const error = new AuditTrailConnectionError('Failed to connect');
      expect(error.cause).toBeUndefined();
    });
  });

  describe('AuditTrailApiError', () => {
    it('should create API error', () => {
      const error = new AuditTrailApiError('Bad request', 400, { field: 'invalid' });
      expect(error.message).toBe('Bad request');
      expect(error.name).toBe('AuditTrailApiError');
      expect(error.statusCode).toBe(400);
      expect(error.body).toEqual({ field: 'invalid' });
    });
  });

  describe('AuditTrailValidationError', () => {
    it('should create validation error', () => {
      const violations = ['Field A is required', 'Field B is invalid'];
      const error = new AuditTrailValidationError('Validation failed', violations);
      expect(error.message).toBe('Validation failed');
      expect(error.name).toBe('AuditTrailValidationError');
      expect(error.violations).toEqual(violations);
    });
  });
});

describe('Validation Utils', () => {
  describe('validateEvent', () => {
    it('should pass for valid event', () => {
      const event = new EventBuilder()
        .actor(ActorBuilder.user('user-1', 'Test'))
        .action(ActionBuilder.create())
        .resource(ResourceBuilder.document('doc-1'))
        .metadata(new MetadataBuilder().source('test').tenantId('tenant-1').build())
        .build();

      expect(() => validateEvent(event)).not.toThrow();
    });

    it('should throw for missing actor', () => {
      const event = {
        action: ActionBuilder.create(),
        resource: ResourceBuilder.document('doc-1'),
        metadata: new MetadataBuilder().source('test').tenantId('tenant-1').build()
      } as any;

      expect(() => validateEvent(event)).toThrow(AuditTrailValidationError);
    });

    it('should throw for missing actor id', () => {
      const event = {
        actor: { type: 'USER' },
        action: ActionBuilder.create(),
        resource: ResourceBuilder.document('doc-1'),
        metadata: new MetadataBuilder().source('test').tenantId('tenant-1').build()
      } as any;

      expect(() => validateEvent(event)).toThrow(AuditTrailValidationError);
    });

    it('should throw for missing action', () => {
      const event = {
        actor: ActorBuilder.user('user-1'),
        resource: ResourceBuilder.document('doc-1'),
        metadata: new MetadataBuilder().source('test').tenantId('tenant-1').build()
      } as any;

      expect(() => validateEvent(event)).toThrow(AuditTrailValidationError);
    });

    it('should throw for missing resource', () => {
      const event = {
        actor: ActorBuilder.user('user-1'),
        action: ActionBuilder.create(),
        metadata: new MetadataBuilder().source('test').tenantId('tenant-1').build()
      } as any;

      expect(() => validateEvent(event)).toThrow(AuditTrailValidationError);
    });

    it('should throw for missing metadata', () => {
      const event = {
        actor: ActorBuilder.user('user-1'),
        action: ActionBuilder.create(),
        resource: ResourceBuilder.document('doc-1')
      } as any;

      expect(() => validateEvent(event)).toThrow(AuditTrailValidationError);
    });

    it('should throw for missing metadata source', () => {
      const event = {
        actor: ActorBuilder.user('user-1'),
        action: ActionBuilder.create(),
        resource: ResourceBuilder.document('doc-1'),
        metadata: { tenantId: 'tenant-1' }
      } as any;

      expect(() => validateEvent(event)).toThrow(AuditTrailValidationError);
    });

    it('should collect multiple violations', () => {
      const event = {
        actor: {},
        action: {},
        resource: {},
        metadata: {}
      } as any;

      try {
        validateEvent(event);
        expect.fail('Should have thrown');
      } catch (error) {
        expect(error instanceof AuditTrailValidationError).toBe(true);
        const validationError = error as AuditTrailValidationError;
        expect(validationError.violations.length).toBeGreaterThan(1);
      }
    });
  });

  describe('isValidUUID', () => {
    it('should return true for valid UUID v4', () => {
      expect(isValidUUID('550e8400-e29b-41d4-a716-446655440000')).toBe(true);
    });

    it('should return true for valid UUID v1', () => {
      expect(isValidUUID('6ba7b810-9dad-11d1-80b4-00c04fd430c8')).toBe(true);
    });

    it('should return false for invalid UUID', () => {
      expect(isValidUUID('not-a-uuid')).toBe(false);
      expect(isValidUUID('550e8400-e29b-41d4-a716')).toBe(false);
      expect(isValidUUID('')).toBe(false);
    });
  });

  describe('isValidISODate', () => {
    it('should return true for valid ISO date', () => {
      expect(isValidISODate('2024-01-15T10:30:00Z')).toBe(true);
      expect(isValidISODate('2024-01-15T10:30:00.000Z')).toBe(true);
      expect(isValidISODate('2024-01-15')).toBe(true);
    });

    it('should return false for invalid date', () => {
      expect(isValidISODate('not-a-date')).toBe(false);
      expect(isValidISODate('')).toBe(false);
    });
  });
});
