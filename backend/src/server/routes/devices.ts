import { Router } from 'express';
import { getConfig } from '../config';
import { jwtAuth, AuthenticatedRequest } from '../middleware/auth';

const userToTokens = new Map<string, Set<string>>();
export function getUserTokens(userId: string): string[] {
  return Array.from(userToTokens.get(userId) || []);
}

export const devicesRouter = (config: ReturnType<typeof getConfig>) => {
  const r = Router();
  r.use(jwtAuth(config.jwtSecret));

  r.post('/', (req: AuthenticatedRequest, res) => {
    const userId = req.userId!;
    const { fcmToken } = req.body || {};
    if (!fcmToken) return res.status(400).json({ error: 'fcmToken required' });
    const set = userToTokens.get(userId) || new Set<string>();
    set.add(fcmToken);
    userToTokens.set(userId, set);
    return res.json({ ok: true, tokens: set.size });
  });

  return r;
};

