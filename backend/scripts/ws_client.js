const WebSocket = require('ws');

const token = process.env.TOKEN;
const label = process.env.LABEL || 'WS';
if (!token) {
  console.error('Missing TOKEN env');
  process.exit(1);
}

const ws = new WebSocket(`ws://localhost:8080/ws?token=${encodeURIComponent(token)}`);

function log(...args) {
  console.log(`[${label}]`, ...args);
}

ws.on('open', () => {
  log('connected');
});

ws.on('message', (data) => {
  try {
    const msg = JSON.parse(data.toString());
    log('event', msg);
    if (process.env.AUTO_RECEIPTS === '1' && msg && msg.type === 'message:new') {
      const id = msg.message && msg.message.id;
      if (id) {
        ws.send(JSON.stringify({ type: 'receipt', receiptType: 'delivered', messageIds: [id] }));
        setTimeout(() => {
          ws.send(JSON.stringify({ type: 'receipt', receiptType: 'read', messageIds: [id] }));
        }, 200);
      }
    }
  } catch (e) {
    log('invalid json', data.toString());
  }
});

setTimeout(() => {
  log('closing');
  ws.close();
  process.exit(0);
}, Number(process.env.DURATION_MS || 6000));

