Messenger Lite Monorepo
========================

This repository contains:

- backend: TypeScript Express + Socket.IO server (REST + WebSocket + signaling)
- infra: Docker Compose for Postgres, Redis, MinIO, coturn, backend
- openapi: OpenAPI spec (YAML)
- android: Android app skeleton (Jetpack Compose, Hilt, Room, WorkManager)

Quick start (dev)
-----------------

1) Copy env:

```bash
cp .env.example .env
```

2) Start infrastructure and backend:

```bash
docker compose -f infra/docker-compose.yml up --build
```

3) API docs live in `openapi/openapi.yaml`.

4) Android module will be in `android/` (skeleton to be added next).

Services
--------

- Postgres: localhost:5432 (user: app, pass: app, db: messenger)
- Redis: localhost:6379
- MinIO: localhost:9000 (console :9001)
- coturn: UDP/TCP 3478 (auth via TURN_USERNAME/TURN_PASSWORD)
- Backend: http://localhost:8080

Security
--------
- CI (GitHub Actions)
---------------------

- JDK 17 via `actions/setup-java`
- Android SDK via `android-actions/setup-android`
- Gradle wrapper is generated in CI if missing: `gradle/gradle-build-action@v3` with `wrapper` task.
- Jobs:
  - build: `./gradlew assembleDebug` and unit tests
  - connected-tests (macOS + emulator): `connectedDebugAndroidTest`


- Dev-only defaults. Replace secrets in `.env` for staging/production.
- TLS termination should be handled by an ingress/proxy in non-dev environments.

