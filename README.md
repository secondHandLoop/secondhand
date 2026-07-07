# secondhand

## OrbStack full development environment

The project includes an OrbStack-compatible Docker Compose environment. It starts PostgreSQL, MongoDB, Adminer, all backend services, the gateway, the customer webpage, and the admin dashboard.

### Prerequisites

- OrbStack installed and running
- The `secondhand-backend` and `secondhand-Web` directories next to each other

Expected directory layout:

```text
secondhand/
├── secondhand-backend/
└── secondhand-Web/
```

### First start

```bash
cd secondhand-backend

# Only needed when .env does not already exist.
cp .env.orbstack.example .env

docker compose -f compose.orbstack.yml up -d --build
```

The first build downloads the Java, Node.js, and service dependencies, so it takes longer than later starts.

### Service URLs

| Service | URL |
| --- | --- |
| Webpage | `http://localhost:7200` |
| Admin dashboard | `http://localhost:7201` |
| Gateway | `http://localhost:7100` |
| User service | `http://localhost:7101` |
| Note service | `http://localhost:7102` |
| Message service | `http://localhost:7103` |
| Promotion service | `http://localhost:7104` |
| Adminer | `http://localhost:8088` |
| PostgreSQL | `localhost:5432` |
| MongoDB | `localhost:27017` |

For Adminer, use `postgres` as the server, database, username, and password.

### Daily commands

```bash
# Start existing containers.
docker compose -f compose.orbstack.yml up -d

# Show container status.
docker compose -f compose.orbstack.yml ps

# Follow logs from every service.
docker compose -f compose.orbstack.yml logs -f

# Follow one service.
docker compose -f compose.orbstack.yml logs -f user-service

# Rebuild after source code changes.
docker compose -f compose.orbstack.yml up -d --build

# Stop the environment without deleting database data.
docker compose -f compose.orbstack.yml down
```

To completely reset PostgreSQL and MongoDB, including all local data:

```bash
docker compose -f compose.orbstack.yml down -v
```

### iOS with the OrbStack backend

The native iOS app still runs through Xcode on macOS; it is not run inside a container. The backend ports are exposed by OrbStack, so an iOS simulator can use the normal localhost API configuration.

For a physical iPhone, set your Mac's LAN IP in `secondhand-Web/apps/web/.env.local`, then rebuild the Capacitor app:

```dotenv
VITE_API_HOST=192.168.1.100
```

```bash
cd ../secondhand-Web/apps/web
pnpm cap:build
pnpm cap:open:ios
```

The Mac and iPhone must be on the same network. Also add the LAN IP to `server.allowNavigation` in `apps/web/capacitor.config.ts` if it is not already allowed.

## Databases only

If you want to run Java and frontend services directly on macOS, start only the local databases with the smaller existing Compose file:

```bash
docker compose up -d
```

This starts:
- PostgreSQL on `localhost:5432`
- MongoDB on `localhost:27017`
- Adminer on `localhost:8088`

## Auth env vars

Set these before running the backend services:

```bash
export AUTH_JWT_SECRET='replace-with-a-long-random-secret-at-least-32-bytes'
export RESEND_API_KEY='re_xxx'
export AUTH_EMAIL_FROM='Acme <onboarding@resend.dev>'
```

Optional:

```bash
export AUTH_JWT_ISSUER='secondhand'
export AUTH_JWT_ACCESS_TOKEN_EXPIRATION_SECONDS='604800'
export AUTH_EMAIL_VERIFICATION_CODE_EXPIRATION_SECONDS='600'
export AUTH_EMAIL_INACTIVE_LOGIN_THRESHOLD_SECONDS='2592000'
export MONGODB_URI='mongodb://localhost:27017/secondhand_message'
export R2_ENABLED='false'
```

## Build check

```bash
mvn -pl service-common,service-user,service-note,service-message -am compile
```

## Swagger

After starting a service, open its Swagger UI:

- User service: `http://localhost:7101/swagger-ui.html`
- Note service: `http://localhost:7102/swagger-ui.html`
- Message service: `http://localhost:7103/swagger-ui.html`

You can also access the raw OpenAPI JSON:

- User service: `http://localhost:7101/v3/api-docs`
- Note service: `http://localhost:7102/v3/api-docs`
- Message service: `http://localhost:7103/v3/api-docs`

## Auth flow

Registration:

1. `POST /user/register/request-code`
```json
{
  "email": "alice@example.com"
}
```

2. `POST /user/register/verify-code`
```json
{
  "verificationToken": "from-step-1",
  "code": "123456"
}
```

3. `POST /user/register`
```json
{
  "verificationToken": "from-step-2",
  "password": "password123",
  "username": "alice"
}
```

Login:

1. `POST /user/login`
```json
{
  "email": "alice@example.com",
  "password": "password123"
}
```

If the response says `verificationRequired=true`, finish with:

2. `POST /user/login/verify-email`
```json
{
  "verificationToken": "from-login-response",
  "code": "123456"
}
```

Protected requests use:

```http
Authorization: Bearer <accessToken>
```
