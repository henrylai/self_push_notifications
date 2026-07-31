# PushPal — Domain Model

## Entity Relationship Diagram

```
┌─────────────────┐       ┌──────────────────────┐
│      User        │       │   UserRelationship    │
│─────────────────│       │──────────────────────│
│ id: UUID (PK)   │◄──┐   │ id: UUID (PK)        │
│ email: String    │   │   │ inviterId: UUID (FK)  │──┐
│ name: String     │   │   │ inviteeId: UUID (FK)  │──┤
│ authProvider:    │   │   │ inviteCode: String     │  │
│   GOOGLE|EMAIL   │   │   │ status:                │  │
│ createdAt:       │   │   │   PENDING|ACTIVE       │  │
│   Timestamp      │   │   │ expiresAt: Timestamp   │  │
│ updatedAt:       │   │   │ createdAt: Timestamp   │  │
│   Timestamp      │   │   └──────────────────────┘  │
└────────┬────────┘   │                               │
         │            │   ┌────────────────────────┐  │
         │            │   │   PushSubscription      │  │
         │            │   │────────────────────────│  │
         │            │   │ id: UUID (PK)          │  │
         │            ├───│ userId: UUID (FK)       │  │
         │            │   │ endpoint: String        │  │
         │            │   │ p256dh: String          │  │
         │            │   │ auth: String            │  │
         │            │   │ createdAt: Timestamp    │  │
         │            │   └────────────────────────┘  │
         │            │                               │
         │            │   ┌────────────────────────┐  │
         │            │   │   Notification          │  │
         │            │   │────────────────────────│  │
         │            └───│ senderId: UUID (FK)     │  │
         │                │ recipientId: UUID (FK)  │──┘
         │                │ title: String           │
         │                │ body: String (nullable) │
         │                │ scheduledTime:          │
         │                │   Timestamp             │
         │                │ status:                 │
         │                │   PENDING|SENT|         │
         │                │   DELIVERED|VIEWED|     │
         │                │   FAILED|CANCELLED      │
         │                │ createdAt: Timestamp    │
         │                │ updatedAt: Timestamp    │
         │                └────────────────────────┘
```

---

## Entity Definitions

### User

| Field | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK | Unique identifier |
| `email` | String | UNIQUE, NOT NULL | User's email address |
| `name` | String | NOT NULL | Display name |
| `authProvider` | Enum | NOT NULL | `GOOGLE` or `EMAIL` |
| `createdAt` | Timestamp | NOT NULL | Account creation time |
| `updatedAt` | Timestamp | NOT NULL | Last profile update |

**Relationships:**
- Has many `PushSubscription`
- Has many `Notification` (as sender)
- Has many `Notification` (as recipient)
- Has many `UserRelationship` (as inviter)
- Has many `UserRelationship` (as invitee)

---

### UserRelationship

| Field | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK | Unique identifier |
| `inviterId` | UUID | FK → User, NOT NULL | User who generated the invite |
| `inviteeId` | UUID | FK → User, NOT NULL | User who accepted the invite |
| `inviteCode` | String | UNIQUE, NOT NULL | 6-character alphanumeric code |
| `status` | Enum | NOT NULL | `PENDING` or `ACTIVE` |
| `expiresAt` | Timestamp | NOT NULL | Code expiry (7 days) |
| `createdAt` | Timestamp | NOT NULL | Relationship creation time |

**Constraints:**
- `inviterId ≠ inviteeId` (can't invite yourself)
- One active relationship per user
- Invite code is unique and expires after 7 days

---

### PushSubscription

| Field | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK | Unique identifier |
| `userId` | UUID | FK → User, NOT NULL | Subscription owner |
| `endpoint` | String | UNIQUE, NOT NULL | Push service endpoint URL |
| `p256dh` | String | NOT NULL | Encryption key (public) |
| `auth` | String | NOT NULL | Encryption key (auth secret) |
| `createdAt` | Timestamp | NOT NULL | Subscription creation time |

**Constraints:**
- One subscription per endpoint (upsert on re-registration)
- Cascade delete when user is deleted

---

### Notification

| Field | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK | Unique identifier |
| `senderId` | UUID | FK → User, NULLABLE | Sender (null for self-reminders) |
| `recipientId` | UUID | FK → User, NOT NULL | Recipient of notification |
| `title` | String(100) | NOT NULL | Notification title |
| `body` | String(500) | NULLABLE | Notification body |
| `scheduledTime` | Timestamp | NOT NULL | When to deliver |
| `status` | Enum | NOT NULL | Current status |
| `createdAt` | Timestamp | NOT NULL | Creation time |
| `updatedAt` | Timestamp | NOT NULL | Last status update |

**Status Enum:**
```
PENDING → SENT → DELIVERED → VIEWED
    │                           │
    └──→ CANCELLED              │
    └──→ FAILED (after retry)   │
```

**Constraints:**
- `scheduledTime` must be in the future (on create)
- `senderId` can be null (self-reminder)
- `recipientId` must exist in `users`
- `senderId` must exist in `users` (if not null)

---

## State Transitions

### Notification Status

```
        ┌─────────┐
        │ PENDING │◄─── Created
        └────┬────┘
             │
    Scheduler picks up
             │
             ▼
        ┌────┐
        │SENT│
        └──┬─┘
           │
   Push delivered
           │
           ▼
      ┌───────────┐
      │ DELIVERED │
      └─────┬─────┘
            │
    User opens notification
            │
            ▼
       ┌────────┐
       │ VIEWED │
       └────────┘

   At any point before SENT:
        │
        └──→ ┌───────────┐
             │ CANCELLED │
             └───────────┘

   After retry failure:
        │
        └──→ ┌────────┐
             │ FAILED │
             └────────┘
```
