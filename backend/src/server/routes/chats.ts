import { Router } from 'express';
import { jwtAuth, AuthenticatedRequest } from '../middleware/auth';
import { getConfig } from '../config';
import { store } from '../store/inmem';

export const chatsRouter = (config: ReturnType<typeof getConfig>) => {
  const r = Router();
  r.use(jwtAuth(config.jwtSecret));

  r.get('/', (req: AuthenticatedRequest, res) => {
    const userId = req.userId!;
    const chats = store.listChatsForUser(userId).map(c => ({ id: c.id, members: c.members, createdAt: c.createdAt }));
    res.json({ chats });
  });

  r.post('/with', (req: AuthenticatedRequest, res) => {
    const userId = req.userId!;
    const { userId: otherId } = req.body || {};
    if (!otherId) return res.status(400).json({ error: 'userId required' });
    const chat = store.getOrCreateChatForUsers(userId, otherId);
    res.json({ chatId: chat.id });
  });

  r.get('/:id/messages', (req: AuthenticatedRequest, res) => {
    const userId = req.userId!;
    const { id } = req.params;
    const after = req.query.after ? Number(req.query.after) : undefined;
    const chat = store.listChatsForUser(userId).find(c => c.id === id);
    if (!chat) return res.status(404).json({ error: 'chat not found' });
    const messages = store.getMessages(id, after);
    res.json({ messages });
  });

  return r;
};

