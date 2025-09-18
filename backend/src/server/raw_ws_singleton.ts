type Sender = (userId: string, data: any) => void;
let senderRef: Sender | null = null;
export function setRawSender(fn: Sender) { senderRef = fn; }
export function sendToUserRaw(userId: string, data: any) { if (senderRef) senderRef(userId, data); }

