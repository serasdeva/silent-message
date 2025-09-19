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

async function main() {
  const TA = tokenFor('user_a');
  const TB = tokenFor('user_b');
  const TC = tokenFor('user_c');

  const chatAB = await ensureChat(TA, 'user_b');
  const chatAC = await ensureChat(TA, 'user_c');

  function connect(token, label) {
    const ws = new WebSocket(`ws://localhost:8080/ws?token=${encodeURIComponent(token)}`);
    ws.on('open', () => console.log(`[${label}] open`));
    ws.on('message', (d) => {
      try {
        const m = JSON.parse(d.toString());
        console.log(`[${label}]`, m);
      } catch {}
    });
    return ws;
  }

  const wsA = connect(TA, 'A');
  const wsB = connect(TB, 'B');
  const wsC = connect(TC, 'C');

  await new Promise(r => setTimeout(r, 500));

  wsB.send(JSON.stringify({ type: 'typing', chatId: chatAB, isTyping: true }));
  wsC.send(JSON.stringify({ type: 'typing', chatId: chatAC, isTyping: true }));

  await new Promise(r => setTimeout(r, 1200));
  wsB.send(JSON.stringify({ type: 'typing', chatId: chatAB, isTyping: false }));
  wsC.send(JSON.stringify({ type: 'typing', chatId: chatAC, isTyping: false }));

  await new Promise(r => setTimeout(r, 800));
  wsA.close(); wsB.close(); wsC.close();
}

main().catch(e => { console.error(e); process.exit(1); });

