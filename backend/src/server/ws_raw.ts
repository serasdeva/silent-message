import { WebSocketServer, WebSocket } from 'ws';
import { IncomingMessage } from 'http';
import jwt from 'jsonwebtoken';
import { store } from './store/inmem';

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
    ws.on('message', (data: any) => {
      try {
        const msg = JSON.parse(data.toString());
        const ctx = (ws as any).ctx as Ctx;
        if (!ctx) return;
        if (msg.type === 'typing') {
          const chat = store.listChatsForUser(ctx.userId).find(c => c.id === msg.chatId);
          if (!chat) return;
          for (const member of chat.members) {
            if (member !== ctx.userId) {
              (wss.clients as any).forEach((client: any) => {
                if (client.readyState === WebSocket.OPEN && client.ctx?.userId === member) {
                  client.send(JSON.stringify({ type: 'typing', chatId: msg.chatId, userId: ctx.userId, isTyping: !!msg.isTyping }));
                }
              });
            }
          }
        }
        if (msg.type === 'receipt') {
          const updates = store.updateReceipts(msg.messageIds || [], ctx.userId, msg.receiptType || 'delivered');
          for (const upd of updates) {
            const m = store.findMessageById(upd.messageId);
            if (!m) continue;
            const chat = store.listChatsForUser(ctx.userId).find(c => c.id === m.chatId);
            if (!chat) continue;
            for (const member of chat.members) {
              (wss.clients as any).forEach((client: any) => {
                if (client.readyState === WebSocket.OPEN && client.ctx?.userId === member) {
                  client.send(JSON.stringify({ type: 'receipt:update', messageId: upd.messageId, userId: upd.userId, deliveredAt: upd.deliveredAt, readAt: upd.readAt }));
                }
              });
            }
          }
        }
      } catch {
        // ignore
      }
    });
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

