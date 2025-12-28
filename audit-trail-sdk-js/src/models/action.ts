import { Action } from '../types';

export class ActionBuilder {
  private action: Partial<Action> = {};

  type(type: string): this { this.action.type = type; return this; }
  description(description: string): this { this.action.description = description; return this; }
  category(category: string): this { this.action.category = category; return this; }

  build(): Action {
    if (!this.action.type) {
      throw new Error('Action type is required');
    }
    return this.action as Action;
  }

  // Static factory methods
  static create(description?: string): Action {
    return { type: 'CREATE', description };
  }

  static read(description?: string): Action {
    return { type: 'READ', description };
  }

  static update(description?: string): Action {
    return { type: 'UPDATE', description };
  }

  static delete(description?: string): Action {
    return { type: 'DELETE', description };
  }

  static login(): Action {
    return { type: 'LOGIN', description: 'User login' };
  }

  static logout(): Action {
    return { type: 'LOGOUT', description: 'User logout' };
  }
}
