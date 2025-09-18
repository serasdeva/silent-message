import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';

export interface AuthenticatedRequest extends Request {
  userId?: string;
}

export function jwtAuth(secret: string) {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    const auth = req.headers.authorization || '';
    const token = auth.startsWith('Bearer ') ? auth.slice(7) : undefined;
    if (!token) return res.status(401).json({ error: 'missing token' });
    try {
      const payload = jwt.verify(token, secret) as any;
      if (payload.type !== 'access') return res.status(401).json({ error: 'invalid token type' });
      req.userId = payload.sub as string;
      next();
    } catch {
      return res.status(401).json({ error: 'invalid token' });
    }
  };
}

