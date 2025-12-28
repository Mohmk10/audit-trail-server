import { AuditEvent } from '../types';
import { AuditTrailValidationError } from '../errors/errors';

export function validateEvent(event: AuditEvent): void {
  const violations: string[] = [];

  // Validate actor
  if (!event.actor) {
    violations.push('Actor is required');
  } else {
    if (!event.actor.id) violations.push('Actor id is required');
    if (!event.actor.type) violations.push('Actor type is required');
  }

  // Validate action
  if (!event.action) {
    violations.push('Action is required');
  } else {
    if (!event.action.type) violations.push('Action type is required');
  }

  // Validate resource
  if (!event.resource) {
    violations.push('Resource is required');
  } else {
    if (!event.resource.id) violations.push('Resource id is required');
    if (!event.resource.type) violations.push('Resource type is required');
  }

  // Validate metadata
  if (!event.metadata) {
    violations.push('Metadata is required');
  } else {
    if (!event.metadata.source) violations.push('Metadata source is required');
    if (!event.metadata.tenantId) violations.push('Metadata tenantId is required');
  }

  if (violations.length > 0) {
    throw new AuditTrailValidationError('Event validation failed', violations);
  }
}

export function isValidUUID(value: string): boolean {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegex.test(value);
}

export function isValidISODate(value: string): boolean {
  const date = new Date(value);
  return !isNaN(date.getTime());
}
