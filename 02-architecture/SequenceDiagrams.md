# PushPal — Sequence Diagrams

## 1. Reminder Creation Flow

```
User          Frontend         API            Database        Scheduler
 │               │              │               │               │
 │  Tap "+"      │              │               │               │
 │──────────────►│              │               │               │
 │               │              │               │               │
 │  Fill form    │              │               │               │
 │──────────────►│              │               │               │
 │               │              │               │               │
 │  Tap Schedule │              │               │               │
 │──────────────►│              │               │               │
 │               │  POST /notifications         │               │
 │               │─────────────►│               │               │
 │               │              │  INSERT       │               │
 │               │              │──────────────►│               │
 │               │              │               │               │
 │               │              │  201 Created  │               │
 │               │◄─────────────│               │               │
 │               │              │               │               │
 │  Toast        │              │               │               │
 │◄──────────────│              │               │               │
 │               │              │               │               │
 │               │              │               │  Poll every 30s│
 │               │              │               │◄──────────────│
 │               │              │               │               │
 │               │              │  SELECT due   │               │
 │               │              │◄──────────────│───────────────│
 │               │              │               │               │
 │               │              │  Send Push    │               │
 │               │              │───────────────│──────────────►│
 │               │              │               │               │
 │  Push arrives │              │               │               │
 │◄─────────────────────────────────────────────────────────────│
```

---

## 2. Notification Delivery Flow

```
Scheduler        PushService       Web Push API      Browser/Device
    │                │                 │                  │
    │  Find due      │                 │                  │
    │  notifications │                 │                  │
    │───────────────►│                 │                  │
    │                │                 │                  │
    │                │  Look up        │                  │
    │                │  subscriptions  │                  │
    │                │────────────────►│                  │
    │                │                 │                  │
    │                │  For each       │                  │
    │                │  subscription:  │                  │
    │                │                 │                  │
    │                │  POST /send     │                  │
    │                │────────────────►│                  │
    │                │                 │  Push event      │
    │                │                 │─────────────────►│
    │                │                 │                  │
    │                │                 │  201 OK          │
    │                │◄────────────────│                  │
    │                │                 │                  │
    │  Update status │                 │                  │
    │  to SENT       │                 │                  │
    │◄───────────────│                 │                  │
    │                │                 │                  │
    │                │                 │    ┌─────────────│
    │                │                 │    │ User opens  │
    │                │                 │    │ notification│
    │                │                 │    │             │
    │                │                 │    │ POST /viewed│
    │                │◄────────────────│────│─────────────│
    │                │                 │    │             │
    │                │  Update status  │    │             │
    │                │  to VIEWED      │    │             │
    │                │────────────────►│    │             │
```

---

## 3. Auth Flow (Google OAuth2)

```
User          Frontend         API            Google
 │               │              │               │
 │  Tap "Sign in │              │               │
 │  with Google" │              │               │
 │──────────────►│              │               │
 │               │              │               │
 │               │  Redirect to │               │
 │               │  Google OAuth│               │
 │               │──────────────────────────────►│
 │               │              │               │
 │  Login at     │              │               │
 │  Google       │              │               │
 │──────────────────────────────────────────────►│
 │               │              │               │
 │               │  Auth code   │               │
 │               │◄──────────────────────────────│
 │               │              │               │
 │               │  POST /auth/google            │
 │               │  { code }    │               │
 │               │─────────────►│               │
 │               │              │  Exchange     │
 │               │              │  code for     │
 │               │              │  tokens       │
 │               │              │──────────────►│
 │               │              │               │
 │               │              │  User info    │
 │               │              │◄──────────────│
 │               │              │               │
 │               │              │  Create/update│
 │               │              │  user in DB   │
 │               │              │──────────────►│
 │               │              │               │
 │               │  JWT token   │               │
 │               │◄─────────────│               │
 │               │              │               │
 │  Store JWT    │              │               │
 │  Redirect to  │              │               │
 │  dashboard    │              │               │
 │◄──────────────│              │               │
```

---

## 4. Device Registration Flow

```
User          Frontend         Service Worker    API            Database
 │               │              │               │               │
 │  Allow        │              │               │               │
 │  notifications│              │               │               │
 │──────────────►│              │               │               │
 │               │              │               │               │
 │               │  Subscribe    │               │               │
 │               │─────────────►│               │               │
 │               │              │               │               │
 │               │  Push sub     │               │               │
 │               │◄─────────────│               │               │
 │               │              │               │               │
 │               │  POST /devices               │               │
 │               │  { endpoint,  │               │               │
 │               │    keys }     │               │               │
 │               │─────────────►│               │               │
 │               │              │  Upsert       │               │
 │               │              │──────────────►│               │
 │               │              │               │               │
 │               │  201 Created │               │               │
 │               │◄─────────────│               │               │
 │               │              │               │               │
 │  Ready for    │              │               │               │
 │  push         │              │               │               │
 │◄──────────────│              │               │               │
```

---

## 5. Couple Linking Flow

```
User A         Frontend A       API            Database        Frontend B       User B
 │               │              │               │               │              │
 │  Tap "Share   │              │               │               │              │
 │  invite code" │              │               │               │              │
 │──────────────►│              │               │               │              │
 │               │  POST /relationships/invite  │               │              │
 │               │─────────────►│               │               │              │
 │               │              │  INSERT       │               │              │
 │               │              │──────────────►│               │              │
 │               │              │               │               │              │
 │               │  201 { code }│               │               │              │
 │               │◄─────────────│               │               │              │
 │               │              │               │               │              │
 │  Show code    │              │               │               │              │
 │◄──────────────│              │               │               │              │
 │               │              │               │               │              │
 │  Share code   │              │               │               │              │
 │  (manually)   │              │               │               │              │
 │────────────────────────────────────────────────────────────────────────────►│
 │               │              │               │               │              │
 │               │              │               │  User B enters │              │
 │               │              │               │  code          │              │
 │               │              │               │◄──────────────│──────────────│
 │               │              │               │               │              │
 │               │              │  POST /relationships/accept   │              │
 │               │              │  { code }     │               │              │
 │               │              │◄──────────────│──────────────│              │
 │               │              │               │               │              │
 │               │              │  UPDATE       │               │              │
 │               │              │  relationship │               │              │
 │               │              │──────────────►│               │              │
 │               │              │               │               │              │
 │               │              │  200 OK       │               │              │
 │               │              │───────────────│──────────────►│              │
 │               │              │               │               │              │
 │               │              │               │  Linked!      │              │
 │               │              │               │               │  Toast       │
 │               │              │               │               │─────────────►│
```
