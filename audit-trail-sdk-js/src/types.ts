export interface AuditTrailConfig {
  serverUrl: string;
  apiKey?: string;
  timeout?: number;          // Default: 30000ms
  retryAttempts?: number;    // Default: 3
  retryDelay?: number;       // Default: 1000ms
  headers?: Record<string, string>;
}

export interface Actor {
  id: string;
  type: 'USER' | 'SYSTEM' | 'SERVICE';
  name?: string;
  ip?: string;
  userAgent?: string;
  attributes?: Record<string, string>;
}

export interface Action {
  type: string;
  description?: string;
  category?: string;
}

export interface Resource {
  id: string;
  type: string;
  name?: string;
  before?: Record<string, unknown>;
  after?: Record<string, unknown>;
}

export interface EventMetadata {
  source: string;
  tenantId: string;
  correlationId?: string;
  sessionId?: string;
  tags?: Record<string, string>;
  extra?: Record<string, unknown>;
}

export interface AuditEvent {
  actor: Actor;
  action: Action;
  resource: Resource;
  metadata: EventMetadata;
}

export interface EventResponse {
  id: string;
  timestamp: string;
  hash: string;
  status: string;
}

export interface BatchEventResponse {
  total: number;
  succeeded: number;
  failed: number;
  events: EventResponse[];
  errors?: ErrorDetail[];
}

export interface ErrorDetail {
  index: number;
  message: string;
  violations?: string[];
}

export interface SearchCriteria {
  tenantId: string;
  actorId?: string;
  actorType?: string;
  actionType?: string;
  resourceId?: string;
  resourceType?: string;
  fromDate?: string;
  toDate?: string;
  query?: string;
  page?: number;
  size?: number;
}

export interface SearchResult<T> {
  items: T[];
  totalCount: number;
  page: number;
  size: number;
  totalPages: number;
}
