import { AuditTrailConfig } from '../types';
import { AuditTrailConnectionError, AuditTrailApiError } from '../errors/errors';
import { withRetry } from './retry';

export class HttpClient {
  private config: Required<AuditTrailConfig>;

  constructor(config: AuditTrailConfig) {
    this.config = {
      serverUrl: config.serverUrl.replace(/\/$/, ''),
      apiKey: config.apiKey || '',
      timeout: config.timeout || 30000,
      retryAttempts: config.retryAttempts || 3,
      retryDelay: config.retryDelay || 1000,
      headers: config.headers || {},
    };
  }

  async post<T>(path: string, body: unknown): Promise<T> {
    return withRetry(() => this.request<T>('POST', path, body), this.config);
  }

  async get<T>(path: string): Promise<T> {
    return withRetry(() => this.request<T>('GET', path), this.config);
  }

  private async request<T>(method: string, path: string, body?: unknown): Promise<T> {
    const url = `${this.config.serverUrl}${path}`;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...this.config.headers,
    };

    if (this.config.apiKey) {
      headers['X-API-Key'] = this.config.apiKey;
    }

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.config.timeout);

    try {
      const response = await fetch(url, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
        signal: controller.signal,
      });

      clearTimeout(timeoutId);

      if (!response.ok) {
        const errorBody = await response.text();
        throw new AuditTrailApiError(
          `API request failed: ${response.status} ${response.statusText}`,
          response.status,
          errorBody
        );
      }

      return response.json();
    } catch (error) {
      clearTimeout(timeoutId);
      if (error instanceof AuditTrailApiError) {
        throw error;
      }
      throw new AuditTrailConnectionError(
        `Failed to connect to ${url}`,
        error instanceof Error ? error : undefined
      );
    }
  }
}
