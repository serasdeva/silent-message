create extension if not exists "uuid-ossp";

create table if not exists users (
  id uuid primary key default uuid_generate_v4(),
  phone_e164 text unique not null,
  display_name text,
  avatar_url text,
  created_at timestamptz not null default now()
);

create table if not exists devices (
  id uuid primary key default uuid_generate_v4(),
  user_id uuid not null references users(id) on delete cascade,
  fcm_token text,
  user_agent text,
  created_at timestamptz not null default now(),
  unique(user_id, fcm_token)
);

create table if not exists contacts (
  id bigserial primary key,
  user_id uuid not null references users(id) on delete cascade,
  phone_hash text not null,
  display_name text,
  created_at timestamptz not null default now(),
  unique(user_id, phone_hash)
);

create table if not exists chats (
  id uuid primary key default uuid_generate_v4(),
  is_group boolean not null default false,
  created_at timestamptz not null default now()
);

create table if not exists chat_members (
  chat_id uuid not null references chats(id) on delete cascade,
  user_id uuid not null references users(id) on delete cascade,
  role text not null default 'member',
  primary key(chat_id, user_id)
);

create table if not exists messages (
  id uuid primary key default uuid_generate_v4(),
  chat_id uuid not null references chats(id) on delete cascade,
  sender_id uuid not null references users(id) on delete cascade,
  body text not null,
  created_at timestamptz not null default now(),
  server_seq bigserial not null
);

create index if not exists idx_messages_chat_seq on messages (chat_id, server_seq);

create table if not exists message_receipts (
  message_id uuid not null references messages(id) on delete cascade,
  user_id uuid not null references users(id) on delete cascade,
  delivered_at timestamptz,
  read_at timestamptz,
  primary key(message_id, user_id)
);

create table if not exists calls (
  id uuid primary key default uuid_generate_v4(),
  caller_id uuid not null references users(id) on delete cascade,
  callee_id uuid not null references users(id) on delete cascade,
  status text not null,
  started_at timestamptz not null default now(),
  ended_at timestamptz
);

