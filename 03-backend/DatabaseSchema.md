# PushPal — Database Schema

## Overview

PostgreSQL 15 with 4 tables. Managed by Railway. Migrations via Flyway.

---

## Tables

### users

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    auth_provider VARCHAR(20) NOT NULL CHECK (auth_provider IN ('GOOGLE', 'EMAIL')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
```

---

### user_relationships

```sql
CREATE TABLE user_relationships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inviter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    invitee_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    invite_code VARCHAR(6) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE')),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_no_self_invite CHECK (inviter_id != invitee_id)
);

CREATE INDEX idx_relationships_inviter ON user_relationships(inviter_id);
CREATE INDEX idx_relationships_invitee ON user_relationships(invitee_id);
CREATE INDEX idx_relationships_code ON user_relationships(invite_code);
CREATE INDEX idx_relationships_active ON user_relationships(status) WHERE status = 'ACTIVE';
```

---

### push_subscriptions

```sql
CREATE TABLE push_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    endpoint VARCHAR(2048) NOT NULL UNIQUE,
    p256dh VARCHAR(255) NOT NULL,
    auth_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_user ON push_subscriptions(user_id);
CREATE INDEX idx_subscriptions_endpoint ON push_subscriptions(endpoint);
```

---

### notifications

```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID REFERENCES users(id) ON DELETE SET NULL,
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    body VARCHAR(500),
    scheduled_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'VIEWED', 'FAILED', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id);
CREATE INDEX idx_notifications_sender ON notifications(sender_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_pending ON notifications(scheduled_time)
    WHERE status = 'PENDING';
CREATE INDEX idx_notifications_scheduled ON notifications(scheduled_time, status)
    WHERE status = 'PENDING';
```

---

## Relationships

```
users.id ←── user_relationships.inviter_id
users.id ←── user_relationships.invitee_id
users.id ←── push_subscriptions.user_id
users.id ←── notifications.sender_id
users.id ←── notifications.recipient_id
```

---

## Migration Strategy

- Use Flyway for schema migrations
- Migration files: `V1__create_users.sql`, `V2__create_relationships.sql`, etc.
- Never modify existing migrations
- All changes go in new migration files
- Test migrations against a fresh database

### Migration Order

| Version | File | Description |
|---|---|---|
| V1 | `V1__create_users.sql` | Users table |
| V2 | `V2__create_user_relationships.sql` | Relationships table |
| V3 | `V3__create_push_subscriptions.sql` | Push subscriptions table |
| V4 | `V4__create_notifications.sql` | Notifications table |

---

## Seed Data (Development Only)

```sql
-- Development user
INSERT INTO users (id, email, name, auth_provider)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'dev@pushpal.app', 'Dev User', 'EMAIL');
```
