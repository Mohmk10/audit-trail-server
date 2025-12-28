import {
  AuditTrailConfig,
  AuditEvent,
  EventResponse,
  BatchEventResponse,
  SearchCriteria,
  SearchResult
} from './types';
import { HttpClient } from './http/http-client';
import { AuditTrailApiError } from './errors/errors';

export class AuditTrailClient {
  private http: HttpClient;

  constructor(config: AuditTrailConfig) {
    this.http = new HttpClient(config);
  }

  // Log single event
  async log(event: AuditEvent): Promise<EventResponse> {
    return this.http.post<EventResponse>('/api/v1/events', event);
  }

  // Log multiple events
  async logBatch(events: AuditEvent[]): Promise<BatchEventResponse> {
    return this.http.post<BatchEventResponse>('/api/v1/events/batch', { events });
  }

  // Get event by ID
  async getById(id: string): Promise<EventResponse | null> {
    try {
      return await this.http.get<EventResponse>(`/api/v1/events/${id}`);
    } catch (error) {
      if (error instanceof AuditTrailApiError && error.statusCode === 404) {
        return null;
      }
      throw error;
    }
  }

  // Search events
  async search(criteria: SearchCriteria): Promise<SearchResult<EventResponse>> {
    return this.http.post<SearchResult<EventResponse>>('/api/v1/search', criteria);
  }

  // Quick search
  async quickSearch(query: string, tenantId: string, page = 0, size = 20): Promise<SearchResult<EventResponse>> {
    const params = new URLSearchParams({
      q: query,
      tenantId,
      page: String(page),
      size: String(size),
    });
    return this.http.get<SearchResult<EventResponse>>(`/api/v1/search/quick?${params}`);
  }

  // Static builder
  static builder(): AuditTrailClientBuilder {
    return new AuditTrailClientBuilder();
  }
}

export class AuditTrailClientBuilder {
  private config: Partial<AuditTrailConfig> = {};

  serverUrl(url: string): this {
    this.config.serverUrl = url;
    return this;
  }

  apiKey(key: string): this {
    this.config.apiKey = key;
    return this;
  }

  timeout(ms: number): this {
    this.config.timeout = ms;
    return this;
  }

  retryAttempts(attempts: number): this {
    this.config.retryAttempts = attempts;
    return this;
  }

  retryDelay(ms: number): this {
    this.config.retryDelay = ms;
    return this;
  }

  headers(headers: Record<string, string>): this {
    this.config.headers = headers;
    return this;
  }

  build(): AuditTrailClient {
    if (!this.config.serverUrl) {
      throw new Error('serverUrl is required');
    }
    return new AuditTrailClient(this.config as AuditTrailConfig);
  }
}
