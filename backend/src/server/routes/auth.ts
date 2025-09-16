import { Router } from 'express';
import jwt from 'jsonwebtoken';
import { getConfig } from '../config';

type TokenPair = { accessToken: string; refreshToken: string };

function signTokens(userId: string, cfg: ReturnType<typeof getConfig>): TokenPair {
  const accessToken = jwt.sign({ sub: userId, type: 'access' }, cfg.jwtSecret, { expiresIn: `${cfg.accessTokenTtlMin}m` });
  const refreshToken = jwt.sign({ sub: userId, type: 'refresh' }, cfg.jwtSecret, { expiresIn: `${cfg.refreshTokenTtlDays}d` });
  return { accessToken, refreshToken };
}

export const authRouter = (config: ReturnType<typeof getConfig>) => {
  const r = Router();

  // Dev stubs for OTP flow
  r.post('/otp/request', (req, res) => {
    const { phone } = req.body || {};
    if (!phone) return res.status(400).json({ error: 'phone required' });
    return res.json({ requestId: `req_${Date.now()}` });
  });

  r.post('/otp/verify', (req, res) => {
    const { requestId, code } = req.body || {};
    if (!requestId || !code) return res.status(400).json({ error: 'requestId and code required' });
    const userId = `user_${Math.random().toString(36).slice(2)}`;
    const tokens = signTokens(userId, config);
    return res.json({ ...tokens, user: { id: userId, phone: 'stub', displayName: 'User' } });
  });

  r.post('/token/refresh', (req, res) => {
    const { refreshToken } = req.body || {};
    if (!refreshToken) return res.status(400).json({ error: 'refreshToken required' });
    try {
      const payload = jwt.verify(refreshToken, config.jwtSecret) as any;
      if (payload.type !== 'refresh') throw new Error('invalid');
      const tokens = signTokens(payload.sub, config);
      return res.json(tokens);
    } catch {
      return res.status(401).json({ error: 'invalid refresh token' });
    }
  });

  return r;
};

