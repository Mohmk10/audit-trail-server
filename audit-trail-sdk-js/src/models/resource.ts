import { Resource } from '../types';

export class ResourceBuilder {
  private resource: Partial<Resource> = {};

  id(id: string): this { this.resource.id = id; return this; }
  type(type: string): this { this.resource.type = type; return this; }
  name(name: string): this { this.resource.name = name; return this; }
  before(before: Record<string, unknown>): this { this.resource.before = before; return this; }
  after(after: Record<string, unknown>): this { this.resource.after = after; return this; }

  build(): Resource {
    if (!this.resource.id || !this.resource.type) {
      throw new Error('Resource id and type are required');
    }
    return this.resource as Resource;
  }

  // Static factory methods
  static of(id: string, type: string, name?: string): Resource {
    return { id, type, name };
  }

  static document(id: string, name?: string): Resource {
    return { id, type: 'DOCUMENT', name };
  }

  static user(id: string, name?: string): Resource {
    return { id, type: 'USER', name };
  }

  static transaction(id: string, name?: string): Resource {
    return { id, type: 'TRANSACTION', name };
  }
}
