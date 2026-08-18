# PushPal — Threat Model

## Threat Analysis

| # | Threat | Risk | Mitigation |
|---|---|---|---|
| T1 | Unauthorized API access | High | JWT authentication on all protected endpoints |
| T2 | Subscription hijacking | High | VAPID keys ensure only our server can send |
| T3 | Spam / abuse | Medium | Atomic per-user action rate limits |
| T4 | Stale push subscriptions | Medium | 404/410 responses durably revoke the subscription |
| T5 | SQL injection | High | JPA parameterized queries, no raw SQL |
| T6 | XSS attacks | Medium | React auto-escaping + Content Security Policy |
| T7 | CSRF attacks | Low | JWT in Authorization header (not cookies) |
| T8 | Man-in-the-middle | High | HTTPS enforced everywhere (TLS 1.3) |
| T9 | Data breach | High | Encryption at rest, no content logging, minimal data |
| T10 | Push notification abuse | Medium | Only linked Pals can send to each other |
| T11 | Invite code brute force | Low | 6-char code, rate limiting, 7-day expiry |
| T12 | Magic link interception | Medium | Single-use, 15-minute expiry, sent over TLS |
| T13 | JWT theft | Medium | 7-day expiry, stored in localStorage, HTTPS only |
| T14 | Denial of service | Low | Rate limiting, Railway DDoS protection |
| T15 | Dependency vulnerabilities | Medium | Dependabot, regular updates |

---

## Trust Boundaries

```
┌─────────────────────────────────────────────────┐
│                    INTERNET                       │
│  (Untrusted - public network)                    │
└──────────────────────┬──────────────────────────┘
                       │ HTTPS (TLS 1.3)
┌──────────────────────▼──────────────────────────┐
│                  CDN / EDGE                       │
│  (Railway - static frontend)                     │
└──────────────────────┬──────────────────────────┘
                       │ HTTPS
┌──────────────────────▼──────────────────────────┐
│               APPLICATION LAYER                   │
│  (Railway - Spring Boot API)                     │
│  ┌────────────────────────────────────────────┐  │
│  │  Authentication (JWT validation)           │  │
│  │  Authorization (user ownership)            │  │
│  │  Rate Limiting                             │  │
│  │  Input Validation                          │  │
│  └────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────┘
                       │ Internal network
┌──────────────────────▼──────────────────────────┐
│                  DATA LAYER                       │
│  (Railway - PostgreSQL)                          │
│  - Encrypted at rest                             │
│  - No notification content in logs               │
│  - Automated backups                             │
└─────────────────────────────────────────────────┘
```

---

## Data Classification

| Data | Classification | Storage | Logging |
|---|---|---|---|
| User email | PII | PostgreSQL (encrypted) | Never |
| User name | PII | PostgreSQL (encrypted) | Never |
| JWT token | Secret | localStorage (client) | Never |
| Push subscription | Secret | PostgreSQL (encrypted) | Never |
| Notification title/body | Confidential | PostgreSQL (encrypted) | Never |
| Notification status | Internal | PostgreSQL | ID only |
| VAPID keys | Secret | Railway env vars | Never |
| Google OAuth credentials | Secret | Railway env vars | Never |

---

## Attack Scenarios

### Scenario 1: Stolen JWT

**Attacker obtains a user's JWT token.**

- **Impact:** Full account access
- **Mitigation:** 7-day expiry limits the window; HTTPS prevents interception; logout removes the
  token from that browser. Server-side token revocation is a future hardening item.
- **Detection:** Anomalous IP patterns (future)

### Scenario 2: Push Subscription Theft

**Attacker obtains a user's push subscription keys.**

- **Impact:** Can receive notifications meant for user
- **Mitigation:** VAPID ensures only our server can send; endpoint is device-specific
- **Detection:** Subscription count anomaly (future)

### Scenario 3: Spam Notifications

**Attacker sends mass notifications to a user.**

- **Impact:** Annoyance, potential harassment
- **Mitigation:** Only linked Pals can send; notification creation is limited to 10/hour
- **Detection:** High notification volume (future)

### Scenario 4: Database Breach

**Attacker gains access to PostgreSQL.**

- **Impact:** User data exposure
- **Mitigation:** Encryption at rest; minimal data stored; no notification content in logs
- **Detection:** Railway monitoring, access logs

---

## Security Controls

### Authentication

- JWT tokens with 7-day expiry
- Google OAuth2 (no password storage)
- Magic links with 15-minute expiry, single-use

### Authorization

- Users can only access their own data
- Notification sender/recipient must be the user or their linked Pal
- Relationship verification on every Pal action

### Input Validation

- Server-side validation on all endpoints
- Client-side validation for UX (server is authoritative)
- Max lengths: title (100), body (500), email (255)

### Rate Limiting

| Endpoint | Limit | Window |
|---|---|---|
| Magic-link requests | 5 per email | 1 hour |
| Notification creation | 10 per user | 1 hour |
| Invite code generation | 10 per user | 1 hour |
| Invite acceptance attempts | 20 per user | 1 hour |

### Encryption

- TLS 1.3 for all transit
- AES-256 for data at rest (Railway default)
- VAPID for push notification authentication
