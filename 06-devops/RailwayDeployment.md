# PushPal — Railway Deployment

## Project Structure

Railway project: `pushpal`

| Service | Type | Description |
|---|---|---|
| `pushpal-api` | Docker / Spring Boot | Backend API |
| `pushpal-db` | PostgreSQL | Database |

Frontend is deployed separately on Vercel (static export).

---

## Service Configuration

### pushpal-api

| Setting | Value |
|---|---|
| Build | Dockerfile in `backend/` |
| Start | `java -jar app.jar` |
| Port | 8080 (Railway sets PORT env var) |
| Health check | `/actuator/health` |
| Memory | 512MB (start), scale as needed |

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### pushpal-db

| Setting | Value |
|---|---|
| Engine | PostgreSQL 15 |
| Plan | Starter (expand as needed) |
| Backups | Automatic daily |
| Connection | Via `DATABASE_URL` env var |

---

## Environment Variables

### pushpal-api

Set in Railway dashboard:

| Variable | Source |
|---|---|
| `DATABASE_URL` | Auto-linked from pushpal-db |
| `DB_USERNAME` | From pushpal-db |
| `DB_PASSWORD` | From pushpal-db |
| `JWT_SECRET` | Manual (generate with `openssl rand -hex 32`) |
| `VAPID_PUBLIC_KEY` | Manual |
| `VAPID_PRIVATE_KEY` | Manual |
| `VAPID_SUBJECT` | Manual (`mailto:pushpal@example.com`) |
| `GOOGLE_CLIENT_ID` | Manual |
| `GOOGLE_CLIENT_SECRET` | Manual |
| `MAGIC_LINK_SECRET` | Manual (generate with `openssl rand -hex 32`) |
| `SPRING_PROFILES_ACTIVE` | `prod` |

---

## Deployment Flow

```
git push origin main
    |
    v
Railway detects push
    |
    v
Builds Docker image (backend/)
    |
    v
Runs Flyway migrations
    |
    v
Starts Spring Boot app
    |
    v
Health check passes
    |
    v
Traffic routed to new instance
```

---

## Build Process

1. Railway pulls latest code from `main`
2. Builds Docker image using `Dockerfile`
3. Runs `./gradlew bootJar` in Docker build
4. Copies JAR to runtime image
5. Deploys and starts
6. Flyway runs any pending migrations
7. Health check at `/actuator/health`

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
| **Total** | | **~$10/month** |

Expand as user base grows.
