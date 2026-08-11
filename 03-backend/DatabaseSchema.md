# PushPal — Database Schema

PostgreSQL 15, managed exclusively through Flyway migrations in
`backend/src/main/resources/db/migration`.

## `users`

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    auth_provider VARCHAR(20) NOT NULL,
    auth_provider_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## `user_relationships`

```sql
CREATE TABLE user_relationships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inviter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    invitee_id UUID REFERENCES users(id) ON DELETE SET NULL,
    invite_code VARCHAR(6) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

Application statuses are `PENDING` and `ACCEPTED`. Indexed columns are `inviter_id`, `invitee_id`,
and pending `invite_code` values.

## `magic_link_tokens`

```sql
CREATE TABLE magic_link_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

Only SHA-256 token hashes are stored. Raw magic-link tokens must never be persisted or logged.

## `push_subscriptions`

```sql
CREATE TABLE push_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    endpoint TEXT NOT NULL,
    p256dh TEXT NOT NULL,
    auth_key TEXT NOT NULL,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

`endpoint` is globally unique, and `user_id` is indexed. Subscription endpoint and key columns are
internal delivery credentials and must not be returned by public API responses.

## `notifications`

```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID REFERENCES users(id) ON DELETE SET NULL,
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    body VARCHAR(500),
    scheduled_time TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    viewed_at TIMESTAMPTZ,
    failure_reason TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ
);
```

Statuses are `PENDING`, `SENT`, `DELIVERED`, `VIEWED`, `FAILED`, and `CANCELLED`.

Indexes support recipient and sender history, due-notification polling, retry scheduling, and the
per-sender rolling-hour rate-limit query:

- `notifications(recipient_id)`
- `notifications(sender_id)`
- `notifications(status, scheduled_time) WHERE status = 'PENDING'`
- `notifications(status, next_attempt_at) WHERE status = 'PENDING'`
- `notifications(sender_id, created_at)`

## Migration history

| Version | File | Purpose |
|---|---|---|
| V1 | `V1__initial_schema.sql` | Core users, relationships, subscriptions, and notifications |
| V2 | `V2__magic_link_tokens.sql` | Hashed single-use magic-link tokens |
| V3 | `V3__notification_retry.sql` | Retry count, next-attempt timestamp, and retry index |
| V4 | `V4__add_notification_rate_limit_index.sql` | Rolling-hour sender rate-limit index |

Never edit an applied migration. Add a new versioned migration for every schema change.
