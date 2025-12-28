export class AuditTrailError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'AuditTrailError';
  }
}

export class AuditTrailConnectionError extends AuditTrailError {
  constructor(message: string, public readonly cause?: Error) {
    super(message);
    this.name = 'AuditTrailConnectionError';
  }
}

export class AuditTrailApiError extends AuditTrailError {
  constructor(
    message: string,
    public readonly statusCode: number,
    public readonly body?: unknown
  ) {
    super(message);
    this.name = 'AuditTrailApiError';
  }
}

export class AuditTrailValidationError extends AuditTrailError {
  constructor(message: string, public readonly violations: string[]) {
    super(message);
    this.name = 'AuditTrailValidationError';
  }
}
