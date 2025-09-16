import http from 'http';
import { Server } from 'socket.io';
import { createApp } from './server/app';
import { registerWebSocketHandlers } from './server/ws';
import { getConfig } from './server/config';

const config = getConfig();
const app = createApp(config);
const server = http.createServer(app);

const io = new Server(server, {
  cors: {
    origin: config.clientOrigin,
    methods: ['GET', 'POST']
  }
});

registerWebSocketHandlers(io, config);

server.listen(config.port, () => {
  // eslint-disable-next-line no-console
  console.log(`Backend listening on http://0.0.0.0:${config.port}`);
});

