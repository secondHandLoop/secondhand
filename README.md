# secondhand

## Local services

Start the local databases:

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
