import { WebSocketServer, WebSocket } from 'ws';
import { IncomingMessage } from 'http';
import jwt from 'jsonwebtoken';

type Ctx = { userId: string };

export function attachRawWebSocket(server: any, jwtSecret: string) {
  const wss = new WebSocketServer({ noServer: true, path: '/ws' });

  function authenticate(req: IncomingMessage): Ctx | null {
    const url = new URL(req.url || '', `http://${req.headers.host}`);
    const token = url.searchParams.get('token') || undefined;
    if (!token) return null;
    try {
      const payload = jwt.verify(token, jwtSecret) as any;
      if (payload.type !== 'access') return null;
      return { userId: payload.sub };
    } catch { return null; }
  }

  (server as any).on('upgrade', (request: IncomingMessage, socket: any, head: any) => {
    if (!request.url?.startsWith('/ws')) return;
    const ctx = authenticate(request);
    if (!ctx) {
      socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n');
      socket.destroy();
      return;
    }
    wss.handleUpgrade(request, socket, head, (ws) => {
      (ws as any).ctx = ctx;
      wss.emit('connection', ws, request);
    });
  });

  wss.on('connection', (ws: WebSocket) => {
    ws.on('message', () => {});
  });

  function sendToUser(userId: string, data: any) {
    const payload = JSON.stringify(data);
    wss.clients.forEach((client: any) => {
      if (client.readyState === WebSocket.OPEN && client.ctx?.userId === userId) {
        client.send(payload);
      }
    });
  }

  return { sendToUser };
}

