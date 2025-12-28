import { AuditEvent, Actor, Action, Resource, EventMetadata } from '../types';

export class EventBuilder {
  private event: Partial<AuditEvent> = {};

  actor(actor: Actor): this { this.event.actor = actor; return this; }
  action(action: Action): this { this.event.action = action; return this; }
  resource(resource: Resource): this { this.event.resource = resource; return this; }
  metadata(metadata: EventMetadata): this { this.event.metadata = metadata; return this; }

  build(): AuditEvent {
    if (!this.event.actor || !this.event.action || !this.event.resource || !this.event.metadata) {
      throw new Error('Actor, action, resource, and metadata are required');
    }
    return this.event as AuditEvent;
  }
}
