import { Server, Socket } from 'socket.io';
import jwt from 'jsonwebtoken';
import { getConfig } from './config';

function authenticateSocket(token: string | undefined, secret: string): { userId: string } | null {
  if (!token) return null;
  try {
    const payload = jwt.verify(token, secret) as any;
    if (payload.type !== 'access') return null;
    return { userId: payload.sub };
  } catch {
    return null;
  }
}

export function registerWebSocketHandlers(io: Server, config: ReturnType<typeof getConfig>) {
  io.on('connection', (socket: Socket) => {
    let userId: string | null = null;

    socket.on('auth', ({ accessToken }: { accessToken: string }) => {
      const auth = authenticateSocket(accessToken, config.jwtSecret);
      if (!auth) {
        socket.emit('error', { error: 'unauthorized' });
        socket.disconnect(true);
        return;
      }
      userId = auth.userId;
      socket.join(`user:${userId}`);
      socket.emit('ready', { userId });
    });

    socket.on('ping', () => socket.emit('pong'));

    socket.on('disconnect', () => {
      if (userId) {
        // cleanup hooks here later
      }
    });
  });
}

