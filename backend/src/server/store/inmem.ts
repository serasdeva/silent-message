import crypto from 'crypto';

export type User = { id: string; displayName: string };
export type Chat = { id: string; members: string[]; createdAt: number };
export type Message = {
  id: string;
  chatId: string;
  senderId: string;
  body: string;
  createdAt: number;
  serverSeq: number;
  clientId?: string;
};
export type Receipt = { messageId: string; userId: string; deliveredAt?: number; readAt?: number };

function uuid() { return crypto.randomUUID(); }

export class InMemoryStore {
  private users: Map<string, User> = new Map();
  private chats: Map<string, Chat> = new Map();
  private chatMembers: Map<string, Set<string>> = new Map();
  private messagesByChat: Map<string, Message[]> = new Map();
  private messageById: Map<string, Message> = new Map();
  private receiptsByMessage: Map<string, Map<string, Receipt>> = new Map();
  private seqByChat: Map<string, number> = new Map();
  private messageByClientId: Map<string, Message> = new Map();

  ensureUser(userId: string) {
    if (!this.users.has(userId)) this.users.set(userId, { id: userId, displayName: `User ${userId.slice(-4)}` });
  }

  getOrCreateChatForUsers(a: string, b: string): Chat {
    for (const chat of this.chats.values()) {
      if (chat.members.length === 2 && new Set(chat.members).size === 2 && chat.members.includes(a) && chat.members.includes(b)) {
        return chat;
      }
    }
    const chat: Chat = { id: uuid(), members: [a, b], createdAt: Date.now() };
    this.chats.set(chat.id, chat);
    this.chatMembers.set(chat.id, new Set(chat.members));
    return chat;
  }

  listChatsForUser(userId: string): Chat[] {
    return Array.from(this.chats.values()).filter(c => c.members.includes(userId));
  }

  appendMessage(chatId: string, senderId: string, body: string, clientId?: string): Message {
    if (clientId && this.messageByClientId.has(clientId)) {
      return this.messageByClientId.get(clientId)!;
    }
    const seq = (this.seqByChat.get(chatId) || 0) + 1;
    this.seqByChat.set(chatId, seq);
    const message: Message = { id: uuid(), chatId, senderId, body, createdAt: Date.now(), serverSeq: seq, clientId };
    const arr = this.messagesByChat.get(chatId) || [];
    arr.push(message);
    this.messagesByChat.set(chatId, arr);
    this.messageById.set(message.id, message);
    if (clientId) this.messageByClientId.set(clientId, message);
    return message;
  }

  getMessages(chatId: string, afterSeq?: number, limit = 50): Message[] {
    const arr = this.messagesByChat.get(chatId) || [];
    const filtered = afterSeq ? arr.filter(m => m.serverSeq > afterSeq) : arr;
    return filtered.slice(0, limit);
  }

  findMessageById(messageId: string): Message | undefined {
    return this.messageById.get(messageId);
  }

  updateReceipts(messageIds: string[], userId: string, type: 'delivered'|'read'): Receipt[] {
    const updated: Receipt[] = [];
    for (const messageId of messageIds) {
      const mp = this.receiptsByMessage.get(messageId) || new Map<string, Receipt>();
      let r = mp.get(userId) || { messageId, userId };
      if (type === 'delivered') r = { ...r, deliveredAt: Date.now() };
      if (type === 'read') r = { ...r, readAt: Date.now() };
      mp.set(userId, r);
      this.receiptsByMessage.set(messageId, mp);
      updated.push(r);
    }
    return updated;
  }
}

export const store = new InMemoryStore();

