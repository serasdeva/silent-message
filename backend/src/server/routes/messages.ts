import { Router } from 'express';
import { jwtAuth, AuthenticatedRequest } from '../middleware/auth';
import { getConfig } from '../config';
import { store } from '../store/inmem';
import { getIo } from '../ws_singleton';
import { sendToUserRaw } from '../raw_ws_singleton';
import { attachRawWebSocket } from '../ws_raw';

export const messagesRouter = (config: ReturnType<typeof getConfig>) => {
  const r = Router();
  r.use(jwtAuth(config.jwtSecret));

  r.post('/', (req: AuthenticatedRequest, res) => {
    const userId = req.userId!;
    const { chatId, clientId, body } = req.body || {};
    if (!chatId || !body) return res.status(400).json({ error: 'chatId and body required' });
    const chat = store.listChatsForUser(userId).find(c => c.id === chatId);
    if (!chat) return res.status(404).json({ error: 'chat not found' });
    const msg = store.appendMessage(chatId, userId, body, clientId);

    const io = getIo();
    for (const member of chat.members) {
      io.to(`user:${member}`).emit('message:new', { message: msg });
      sendToUserRaw(member, { type: 'message:new', message: msg });
    }
    res.json({ messageId: msg.id, serverSeq: msg.serverSeq, createdAt: msg.createdAt });
  });

  r.post('/receipts', (req: AuthenticatedRequest, res) => {
    const userId = req.userId!;
    const { messageIds, type } = req.body || {};
    if (!Array.isArray(messageIds) || !type) return res.status(400).json({ error: 'messageIds[] and type required' });
    const receipts = store.updateReceipts(messageIds, userId, type);
    res.json({ updated: receipts.length });
  });

  return r;
};

