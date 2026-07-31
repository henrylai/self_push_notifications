# PushPal — Secrets Management

## Principle

All secrets are stored as environment variables in Railway. No secrets are committed to the repository. No secrets appear in logs.

---

## Secrets Table

| Secret | Description | Location | Rotation |
|---|---|---|---|
| `DATABASE_URL` | PostgreSQL connection string | Railway env | Automatic (Railway) |
| `DB_USERNAME` | Database username | Railway env | On compromise |
| `DB_PASSWORD` | Database password | Railway env | On compromise |
| `JWT_SECRET` | JWT signing key (256-bit) | Railway env | Every 90 days |
| `VAPID_PUBLIC_KEY` | Web Push VAPID public key | Railway env + frontend | On compromise |
| `VAPID_PRIVATE_KEY` | Web Push VAPID private key | Railway env | On compromise |
| `VAPID_SUBJECT` | VAPID subject (mailto:) | Railway env | Never |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | Railway env | On compromise |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | Railway env | On compromise |
| `MAGIC_LINK_SECRET` | Magic link token signing key | Railway env | Every 90 days |
| `SMTP_HOST` | Email server host | Railway env | Never |
| `SMTP_PORT` | Email server port | Railway env | Never |
| `SMTP_USERNAME` | Email server username | Railway env | On compromise |
| `SMTP_PASSWORD` | Email server password | Railway env | On compromise |
| `SENTRY_DSN` | Sentry error tracking DSN | Railway env | On compromise |

---

## Environment Configuration

### Railway Variables

Set in Railway dashboard under Service > Variables:

```
DATABASE_URL=postgresql://user:pass@host:5432/pushpal
DB_USERNAME=pushpal
DB_PASSWORD=<secure-random>
JWT_SECRET=<256-bit-random-hex>
VAPID_PUBLIC_KEY=<vapid-public>
VAPID_PRIVATE_KEY=<vapid-private>
VAPID_SUBJECT=mailto:pushpal@example.com
GOOGLE_CLIENT_ID=<google-client-id>
GOOGLE_CLIENT_SECRET=<google-client-secret>
MAGIC_LINK_SECRET=<256-bit-random-hex>
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=notifications@pushpal.app
SMTP_PASSWORD=<smtp-password>
```

### Frontend Variables (Public Only)

Only `VAPID_PUBLIC_KEY` is needed on the frontend. It is **not** a secret.

```typescript
// lib/push.ts
const VAPID_PUBLIC_KEY = process.env.NEXT_PUBLIC_VAPID_PUBLIC_KEY;
```

---

## Secret Generation

### JWT Secret

```bash
openssl rand -hex 32
```

### Magic Link Secret

```bash
openssl rand -hex 32
```

### VAPID Keys

```bash
npx web-push generate-vapid-keys
```

---

## Security Rules

1. **Never commit secrets** to Git
2. **Never log secrets** in application logs
3. **Never expose secrets** in API responses
4. **Never store secrets** in localStorage
5. **Never include secrets** in error messages
6. **Use Railway env vars** for all production secrets
7. **Use `.env` file** for local development only (gitignored)

---

## Gitignore

```gitignore
# Environment files
.env
.env.local
.env.*.local

# Never commit these
*.pem
*.key
```

---

## Audit Checklist

- [ ] No secrets in source code
- [ ] No secrets in Git history
- [ ] No secrets in logs
- [ ] No secrets in error messages
- [ ] `.env` files are gitignored
- [ ] Railway env vars are set correctly
- [ ] Secrets are unique per environment (dev/staging/prod)
