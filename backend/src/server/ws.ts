import { Server, Socket } from 'socket.io';
import jwt from 'jsonwebtoken';
import { getConfig } from './config';
import { store } from './store/inmem';
import { setIo } from './ws_singleton';

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
  setIo(io);
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

    socket.on('message:send', ({ chatId, clientId, body }: { chatId: string; clientId?: string; body: string }) => {
      if (!userId) return;
      const chat = store.listChatsForUser(userId).find(c => c.id === chatId);
      if (!chat) return;
      const msg = store.appendMessage(chatId, userId, body, clientId);
      socket.emit('message:ack', { clientId, messageId: msg.id, serverSeq: msg.serverSeq });
      for (const member of chat.members) {
        io.to(`user:${member}`).emit('message:new', { message: msg });
      }
    });

    socket.on('typing', ({ chatId, isTyping }: { chatId: string; isTyping: boolean }) => {
      if (!userId) return;
      const chat = store.listChatsForUser(userId).find(c => c.id === chatId);
      if (!chat) return;
      for (const member of chat.members) {
        if (member !== userId) io.to(`user:${member}`).emit('typing', { chatId, userId, isTyping });
      }
    });

    socket.on('receipt', ({ messageIds, type }: { messageIds: string[]; type: 'delivered'|'read' }) => {
      if (!userId) return;
      const updates = store.updateReceipts(messageIds, userId, type);
      for (const upd of updates) {
        const msg = store.findMessageById(upd.messageId);
        if (!msg) continue;
        const chat = store.listChatsForUser(userId).find(c => c.id === msg.chatId);
        if (!chat) continue;
        for (const member of chat.members) {
          io.to(`user:${member}`).emit('receipt:update', { messageId: upd.messageId, userId: upd.userId, deliveredAt: upd.deliveredAt, readAt: upd.readAt });
        }
      }
    });

    socket.on('disconnect', () => {
      if (userId) {
        // cleanup hooks here later
      }
    });
  });
}

