# PushPal — Railway Deployment

## Project Structure

Railway project: `pushpal`

| Service | Type | Description |
|---|---|---|
| `pushpal-api` | Docker / Spring Boot | Backend API |
| `pushpal-db` | PostgreSQL | Database |
| `pushpal-web` | Docker / static export | Frontend PWA |

All three services live in the same Railway project (private networking between them requires the same project **and** environment).

---

## Service Configuration

### pushpal-api

| Setting | Value |
|---|---|
| Build | Dockerfile in `backend/` (`backend/railway.json`) |
| Start | `java -jar app.jar` |
| Port | Railway `PORT` env var (`application.yml` → `server.port: ${PORT:8080}`) |
| Health check | `/actuator/health` |

### pushpal-db

| Setting | Value |
|---|---|
| Engine | PostgreSQL |
| Connection | `DATABASE_URL` env var (set on the API, see below) |

### pushpal-web

| Setting | Value |
|---|---|
| Build | Dockerfile in `frontend/` (no `railway.json` startCommand — the Dockerfile `CMD` handles it) |
| Start | `serve -s out -l tcp://0.0.0.0:${PORT:-3000}` |
| Port | Railway `PORT` env var (defaults to 3000) |
| Health check | `/` |
| Build args | `NEXT_PUBLIC_API_URL`, `NEXT_PUBLIC_VAPID_PUBLIC_KEY` |

---

## Dockerfiles

### backend/Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### frontend/Dockerfile

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .

ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_VAPID_PUBLIC_KEY
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL \
    NEXT_PUBLIC_VAPID_PUBLIC_KEY=$NEXT_PUBLIC_VAPID_PUBLIC_KEY

RUN npm run build

FROM node:20-alpine AS runtime
WORKDIR /app
RUN npm install -g serve@14
COPY --from=build /app/out ./out
EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=3s \
  CMD sh -c "wget -q -O /dev/null http://localhost:${PORT:-3000}/ || exit 1"

CMD ["sh", "-c", "serve -s out -l tcp://0.0.0.0:${PORT:-3000}"]
```

---

## Environment Variables

### pushpal-api

Set in Railway dashboard:

| Variable | Source |
|---|---|
| `DATABASE_URL` | Full URL, see format below |
| `JWT_SECRET` | Manual (generate with `openssl rand -hex 32`) |
| `VAPID_PUBLIC_KEY` | Manual |
| `VAPID_PRIVATE_KEY` | Manual |
| `VAPID_SUBJECT` | Manual (`mailto:pushpal@example.com`) |
| `GOOGLE_CLIENT_ID` | Manual (Google Cloud OAuth client) |
| `GOOGLE_CLIENT_SECRET` | Manual (Google Cloud OAuth client) |
| `MAGIC_LINK_BASE_URL` | Manual (`https://pushpal.up.railway.app`) |
| `API_BASE_URL` | Manual — the API's public domain, e.g. `https://<api>.up.railway.app`; enables push "delivered" reporting from the service worker |
| `CORS_ALLOWED_ORIGIN` | Manual (`https://pushpal.up.railway.app`) — REQUIRED for deployed FE→API calls |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` | Manual (optional — enables magic-link emails) |
| `MAIL_FROM` | Manual (optional, e.g. `PushPal <noreply@pushpal.app>`) |

#### `DATABASE_URL` format

The working value uses the Postgres service name as the private host (same project + environment):

```
postgresql://postgres:VBgHen...@postgres:5432/railway?sslmode=require
```

Notes on the URL format (the backend auto-parses it — see `DataSourceConfig`):
- Use `postgresql://` (not `postgresql://user:pass@<domain>` with a `.railway.internal` or public proxy host).
- The database name is required: `postgresql://host:5432` alone is rejected by the PG driver.
- The `?sslmode=require` query param is preserved.
- Credentials in the URL are extracted into `spring.datasource.username` / `spring.datasource.password` automatically.

### pushpal-web

Set in Railway dashboard (also exposed as Docker build args):

| Variable | Source |
|---|---|
| `NEXT_PUBLIC_API_URL` | The API's Railway public domain (e.g. `https://<api>.up.railway.app`) |
| `NEXT_PUBLIC_VAPID_PUBLIC_KEY` | Same VAPID public key as the API |
| `NEXT_PUBLIC_GOOGLE_CLIENT_ID` | Google OAuth client ID (same as API) |

---

## Google OAuth Setup

1. Create an OAuth 2.0 Client ID in the [Google Cloud Console](https://console.cloud.google.com/apis/credentials) (type: **Web application**).
2. Add an Authorized redirect URI: `https://pushpal.up.railway.app/auth/callback`.
3. Set `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` on `pushpal-api`, and `NEXT_PUBLIC_GOOGLE_CLIENT_ID` on `pushpal-web`.
4. Redeploy `pushpal-web` (the client ID is baked into the static build).

Flow: FE redirects to Google → Google calls back with `?code=` → FE posts `{ code, redirectUri }` to `POST /api/auth/google` → API exchanges the code with Google and returns a JWT.

## Magic Link Emails (optional)

Without SMTP credentials, magic links are logged to the API console (e.g. `SMTP not configured — magic link for user@example.com: https://pushpal.up.railway.app/auth/callback?token=...`) so you can still sign in via the Railway logs.

To send real emails, set `SMTP_HOST`, `SMTP_PORT` (587), `SMTP_USERNAME`, `SMTP_PASSWORD`, and optionally `MAIL_FROM` on `pushpal-api`. Magic-link tokens are 64-char, SHA-256-hashed, single-use, and expire after 30 days.

---

## Deployment Flow

```
git push origin main
    |
    v
Railway detects push
    |
    v
Builds Docker images (backend/ and frontend/)
    |
    v
Runs Flyway migrations (API, at startup)
    |
    v
Starts Spring Boot app / serve static export
    |
    v
Health checks pass
    |
    v
Traffic routed to new instance
```

---

## Build Process

1. Railway pulls latest code from `main`
2. Builds Docker images using the respective `Dockerfile`s
3. API: `./gradlew bootJar` in build stage, JAR copied to runtime image
4. Frontend: `npm ci` + `npm run build` in build stage, `out/` copied to runtime image
5. Flyway runs any pending migrations at API startup
6. Health checks at `/actuator/health` (API) and `/` (frontend)

---

## Troubleshooting (lessons learned)

| Symptom | Cause | Fix |
|---|---|---|
| `UnknownHostException: <something>.railway.internal` | Service not in same project/environment, or no healthy deployment | Move services into the same project; ensure the DB has a running deployment |
| `Driver claims to not accept jdbcUrl ... postgresql://host:5432?sslmode=require` | `DATABASE_URL` missing the database name | Add `/railway` before the query string |
| `The connection attempt failed` / `Connect timed out` | DB service down, paused, or on a trial plan | Verify the DB has a healthy deployment; check plan tier |
| `serve: Unknown --listen endpoint scheme (protocol): undefined` | Railway `startCommand` is not shell-expanded, so `$PORT` stays literal | Do NOT set `startCommand` in `frontend/railway.json`; rely on the Dockerfile `CMD` with `${PORT:-3000}` |
| `next start` ignores `PORT` | Next.js doesn't read `PORT` by default | Pass it explicitly (`next start --port ${PORT:-3000}`) or use `serve` for static exports |

---

## Custom Domain

1. Add custom domain in Railway dashboard
2. Configure DNS:
   - `CNAME` record: `api.pushpal.app` → Railway-provided URL
3. Railway auto-provisions SSL certificate
4. Verify with `curl https://api.pushpal.app/actuator/health`

---

## Scaling

### Vertical Scaling

Increase memory/CPU in Railway dashboard as needed.

### Horizontal Scaling

Railway supports horizontal scaling. For MVP, single instance is sufficient.

### When to Scale

| Metric | Threshold | Action |
|---|---|---|
| Memory usage | > 80% | Increase to 1GB |
| CPU usage | > 70% | Increase CPU |
| Response time | > 500ms | Scale horizontally |

---

## Rollback

1. Go to Railway dashboard > pushpal-api > Deployments
2. Find previous working deployment
3. Click "Rollback"
4. Verify health check passes

---

## Monitoring

| Tool | Purpose |
|---|---|
| Railway Dashboard | Resource usage, logs, deployments |
| `/actuator/health` | Application health |
| `/actuator/metrics` | Application metrics |
| Sentry (optional) | Error tracking |

---

## Cost Estimate (MVP)

| Service | Plan | Cost |
|---|---|---|
| pushpal-api | Starter | ~$5/month |
| pushpal-db | Starter | ~$5/month |
| pushpal-web | Hobby (one service) | included |
| **Total** | | **~$10/month** |

Expand as user base grows.
