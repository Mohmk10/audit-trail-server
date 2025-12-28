import { Actor } from '../types';

export class ActorBuilder {
  private actor: Partial<Actor> = {};

  id(id: string): this { this.actor.id = id; return this; }
  type(type: Actor['type']): this { this.actor.type = type; return this; }
  name(name: string): this { this.actor.name = name; return this; }
  ip(ip: string): this { this.actor.ip = ip; return this; }
  userAgent(userAgent: string): this { this.actor.userAgent = userAgent; return this; }
  attributes(attributes: Record<string, string>): this { this.actor.attributes = attributes; return this; }

  build(): Actor {
    if (!this.actor.id || !this.actor.type) {
      throw new Error('Actor id and type are required');
    }
    return this.actor as Actor;
  }

  // Static factory methods
  static user(id: string, name?: string): Actor {
    return { id, type: 'USER', name };
  }

  static system(id: string): Actor {
    return { id, type: 'SYSTEM' };
  }

  static service(id: string, name?: string): Actor {
    return { id, type: 'SERVICE', name };
  }
}
