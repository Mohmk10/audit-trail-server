import { EventMetadata } from '../types';

export class MetadataBuilder {
  private metadata: Partial<EventMetadata> = {};

  source(source: string): this { this.metadata.source = source; return this; }
  tenantId(tenantId: string): this { this.metadata.tenantId = tenantId; return this; }
  correlationId(correlationId: string): this { this.metadata.correlationId = correlationId; return this; }
  sessionId(sessionId: string): this { this.metadata.sessionId = sessionId; return this; }
  tags(tags: Record<string, string>): this { this.metadata.tags = tags; return this; }
  extra(extra: Record<string, unknown>): this { this.metadata.extra = extra; return this; }

  build(): EventMetadata {
    if (!this.metadata.source || !this.metadata.tenantId) {
      throw new Error('Source and tenantId are required');
    }
    return this.metadata as EventMetadata;
  }
}
