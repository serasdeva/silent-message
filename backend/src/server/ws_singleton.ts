import { Server } from 'socket.io';

let ioRef: Server | null = null;
export function setIo(io: Server) { ioRef = io; }
export function getIo(): Server {
  if (!ioRef) { throw new Error('Socket.IO not initialized'); }
  return ioRef;
}

