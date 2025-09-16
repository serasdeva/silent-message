import express from 'express';
import cors from 'cors';
import { json, urlencoded } from 'express';
import { getConfig } from './config';
import { healthRouter } from './routes/health';
import { authRouter } from './routes/auth';

export const createApp = (config: ReturnType<typeof getConfig>) => {
  const app = express();
  app.use(cors({ origin: config.clientOrigin, credentials: true }));
  app.use(urlencoded({ extended: true }));
  app.use(json());

  app.use('/health', healthRouter());
  app.use('/auth', authRouter(config));

  app.get('/', (_req, res) => res.json({ ok: true }));

  return app;
};

