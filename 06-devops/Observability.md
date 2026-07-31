# PushPal — Observability

## Overview

Three pillars: Logs, Metrics, Health. Optional: Error tracking (Sentry), Uptime monitoring (BetterUptime).

---

## 1. Logging

### Approach

- **Structured logging** via Logback (Spring Boot default)
- **stdout** output (Railway captures and displays)
- **No notification content** in logs (privacy policy)

### Log Levels

| Level | Usage |
|---|---|
| ERROR | Unexpected failures, push delivery failures |
| WARN | Rate limiting, subscription cleanup, retries |
| INFO | Request logging, auth events, scheduler cycles |
| DEBUG | Development only (disabled in production) |

### Log Format

```
2025-01-15T10:30:00Z INFO  c.p.auth.AuthController - POST /api/auth/google 200 145ms
2025-01-15T10:30:01Z INFO  c.p.notification.NotificationService - Notification created: abc-123
2025-01-15T10:30:02Z WARN  c.p.push.PushService - Subscription expired, cleaning up: sub-456
2025-01-15T10:30:03Z ERROR c.p.scheduler.SchedulerService - Push delivery failed for notification abc-123
```

### What We Log

| Event | What's Logged |
|---|---|
| HTTP request | Method, path, status, duration |
| Auth events | Login method, success/failure |
| Notification creation | Notification ID (no content) |
| Push delivery | Notification ID, success/failure |
| Subscription cleanup | Subscription ID |
| Scheduler cycle | Duration, notifications processed |

### What We NEVER Log

- Notification title or body
- Push subscription endpoints
- JWT tokens
- User email or name
- Invite codes
- Database credentials

---

## 2. Metrics

### Spring Boot Actuator + Micrometer

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: pushpal-api
```

### Key Metrics

| Metric | Type | Description |
|---|---|---|
| `http_server_requests` | Timer | Request latency by endpoint |
| `scheduler_cycle_duration` | Timer | Scheduler cycle duration |
| `scheduler_notifications_processed` | Counter | Notifications processed |
| `push_delivery_success` | Counter | Successful push deliveries |
| `push_delivery_failed` | Counter | Failed push deliveries |
| `push_subscription_cleaned` | Counter | Expired subscriptions removed |
| `jvm_memory_used` | Gauge | JVM memory usage |
| `hikari_connections_active` | Gauge | Database connection pool |

### Prometheus Endpoint

```
GET /actuator/prometheus
```

Railway captures metrics automatically.

---

## 3. Health Checks

### Spring Boot Actuator

```yaml
management:
  endpoint:
    health:
      show-details: when-authorized
  health:
    db:
      enabled: true
    diskspace:
      enabled: true
```

### Health Check Response

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### Endpoint

```
GET /actuator/health
```

Used by Railway for health monitoring and traffic routing.

---

## 4. Error Tracking (Optional)

### Sentry

```yaml
# application.yml
sentry:
  dsn: ${SENTRY_DSN}
  environment: ${SPRING_PROFILES_ACTIVE}
  release: ${APP_VERSION}
  traces-sample-rate: 0.1
```

### What Sentry Captures

- Unhandled exceptions
- Push delivery failures (with context)
- Auth errors
- Database errors

### What Sentry Does NOT Capture

- Notification content
- User PII
- JWT tokens
- Push subscription data

---

## 5. Uptime Monitoring

### BetterUptime (or similar)

| Check | URL | Interval |
|---|---|---|
| API health | `GET /actuator/health` | 60 seconds |
| API response | `GET /api/users/me` (unauthenticated) | 5 minutes |
| Frontend | `GET https://pushpal.app` | 5 minutes |

### Alert Thresholds

| Condition | Alert |
|---|---|
| Health check fails 3x | Email + SMS |
| API response > 5s | Email |
| Frontend down | Email + SMS |
| SSL cert expiring < 14 days | Email |

---

## 6. Dashboard

### Railway Dashboard

- Real-time logs
- Resource usage (CPU, memory, network)
- Deployment history
- Environment variables

### Grafana (Future)

- Metrics visualization
- Custom dashboards
- Alerting rules
