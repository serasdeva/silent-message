const { WebSocket } = require('ws');
const jwt = require('jsonwebtoken');

const SECRET = 'dev_jwt_secret_change_me';
const BASE = process.env.BASE || 'http://localhost:8080';

function tokenFor(userId) {
  return jwt.sign({ sub: userId, type: 'access' }, SECRET, { expiresIn: '5m' });
}

async function ensureChat(token, otherUserId) {
  const res = await fetch(`${BASE}/chats/with`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId: otherUserId })
  });
  const j = await res.json();
  return j.chatId;
}

async function sendMsg(token, chatId, body, clientId) {
  await fetch(`${BASE}/messages`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ chatId, body, clientId })
  });
}

async function fetchAfter(token, chatId, after) {
  const u = new URL(`${BASE}/chats/${chatId}/messages`);
  if (after) u.searchParams.set('after', String(after));
  const res = await fetch(u, { headers: { 'Authorization': `Bearer ${token}` } });
  const j = await res.json();
  return j.messages || [];
}

async function main() {
  const TA = tokenFor('user_a');
  const TB = tokenFor('user_b');
  const chat = await ensureChat(TA, 'user_b');

  let lastSeq = 0;
  const wsB = new WebSocket(`ws://localhost:8080/ws?token=${encodeURIComponent(TB)}`);
  wsB.on('message', (d) => {
    try {
      const m = JSON.parse(d.toString());
      if (m.type === 'message:new') {
        lastSeq = Math.max(lastSeq, m.message.serverSeq);
        console.log('[B]', 'recv', m.message.serverSeq, m.message.body);
      }
    } catch {}
  });
  await new Promise(r => setTimeout(r, 300));

  // Send one message while connected
  await sendMsg(TA, chat, 'before offline', 'rc-1');
  await new Promise(r => setTimeout(r, 300));

  // Simulate offline: close socket, send two messages
  wsB.close();
  await sendMsg(TA, chat, 'during offline 1', 'rc-2');
  await sendMsg(TA, chat, 'during offline 2', 'rc-3');

  // On reconnect, fetch messages after lastSeq
  const missed = await fetchAfter(TB, chat, lastSeq);
  console.log('[B]', 'missed', missed.map(m => m.body));
}

main().catch(e => { console.error(e); process.exit(1); });

