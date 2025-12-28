import { AuditTrailConfig } from '../types';
import { AuditTrailApiError } from '../errors/errors';

export async function withRetry<T>(
  fn: () => Promise<T>,
  config: Required<AuditTrailConfig>
): Promise<T> {
  let lastError: Error | undefined;

  for (let attempt = 0; attempt < config.retryAttempts; attempt++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error instanceof Error ? error : new Error(String(error));

      // Don't retry on 4xx errors (client errors)
      if (error instanceof AuditTrailApiError && error.statusCode >= 400 && error.statusCode < 500) {
        throw error;
      }

      // Wait before retry with exponential backoff
      if (attempt < config.retryAttempts - 1) {
        const delay = config.retryDelay * Math.pow(2, attempt);
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
  }

  throw lastError;
}
