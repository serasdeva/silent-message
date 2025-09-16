import dotenv from 'dotenv';
dotenv.config();

export type AppConfig = {
  port: number;
  jwtSecret: string;
  accessTokenTtlMin: number;
  refreshTokenTtlDays: number;
  databaseUrl: string;
  redisUrl: string;
  clientOrigin: string;
};

export function getConfig(): AppConfig {
  const port = parseInt(process.env.PORT || '8080', 10);
  const jwtSecret = process.env.JWT_SECRET || 'dev_jwt_secret_change_me';
  const accessTokenTtlMin = parseInt(process.env.ACCESS_TOKEN_TTL_MIN || '15', 10);
  const refreshTokenTtlDays = parseInt(process.env.REFRESH_TOKEN_TTL_DAYS || '30', 10);
  const databaseUrl = process.env.DATABASE_URL || 'postgres://app:app@localhost:5432/messenger';
  const redisUrl = process.env.REDIS_URL || 'redis://localhost:6379/0';
  const clientOrigin = process.env.CLIENT_ORIGIN || 'http://localhost:5173';

  return { port, jwtSecret, accessTokenTtlMin, refreshTokenTtlDays, databaseUrl, redisUrl, clientOrigin };
}

