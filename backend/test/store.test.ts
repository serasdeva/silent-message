import { describe, it, expect } from 'vitest';
import { InMemoryStore } from '../src/server/store/inmem';

describe('InMemoryStore', () => {
  it('creates 1:1 chat once and reuses it', () => {
    const s = new InMemoryStore();
    const c1 = s.getOrCreateChatForUsers('a','b');
    const c2 = s.getOrCreateChatForUsers('a','b');
    expect(c1.id).toBe(c2.id);
    expect(s.listChatsForUser('a').length).toBe(1);
  });

  it('appends messages with incrementing seq and idempotent clientId', () => {
    const s = new InMemoryStore();
    const c = s.getOrCreateChatForUsers('a','b');
    const m1 = s.appendMessage(c.id, 'a', 'hello', 'cid');
    const m2 = s.appendMessage(c.id, 'a', 'hello', 'cid');
    expect(m1.id).toBe(m2.id);
    const m3 = s.appendMessage(c.id, 'a', 'next');
    expect(m3.serverSeq).toBeGreaterThan(m1.serverSeq);
  });

  it('updates receipts delivered and read', () => {
    const s = new InMemoryStore();
    const c = s.getOrCreateChatForUsers('a','b');
    const m = s.appendMessage(c.id, 'a', 'x');
    const d = s.updateReceipts([m.id], 'b', 'delivered');
    expect(d[0].deliveredAt).toBeTruthy();
    const r = s.updateReceipts([m.id], 'b', 'read');
    expect(r[0].readAt).toBeTruthy();
  });
});

