# PushPal — Component Map

## Component Overview

| Component | Responsibility | Technology | Location |
|---|---|---|---|
| API Gateway | HTTP routing, CORS, rate limiting, error handling | Spring Boot (embedded Tomcat) | `backend/src/main/java/.../config/` |
| Auth Service | Google OAuth2, magic link, JWT issuance/validation | Spring Security + OAuth2 | `backend/src/main/java/.../auth/` |
| User Service | User CRUD, profile management | Spring Boot + JPA | `backend/src/main/java/.../user/` |
| Relationship Service | Invite codes, Pal linking | Spring Boot + JPA | `backend/src/main/java/.../relationship/` |
| Notification Service | Notification CRUD, status tracking | Spring Boot + JPA | `backend/src/main/java/.../notification/` |
| Scheduler Service | Poll for due notifications, trigger delivery | Spring @Scheduled | `backend/src/main/java/.../scheduler/` |
| Push Service | Send Web Push notifications | Web Push (VAPID) | `backend/src/main/java/.../push/` |
| Frontend | PWA UI, service worker, push subscription | Next.js 16 + React 19 | `frontend/` |
| Database | Persistent storage | PostgreSQL 15 | Railway (managed) |

---

## Component Details

### API Gateway

**Responsibilities:**
- Route HTTP requests to appropriate service
- Handle CORS configuration
- Apply rate limiting (10 requests/hour for unauthenticated)
- Global exception handling
- Request/response logging (no content)

**Tech:** Spring Boot WebMVC, `@RestControllerAdvice`

---

### Auth Service

**Responsibilities:**
- Google OAuth2 flow (authorization code grant)
- Magic link generation and verification
- JWT token issuance (7-day expiry)
- JWT validation on protected routes
- User creation on first sign-in

**Tech:** Spring Security, OAuth2 Client, JWT (jjwt)

---

### User Service

**Responsibilities:**
- Create/update user profiles
- Get current user info
- Update user settings

**Tech:** Spring Data JPA, User entity

---

### Relationship Service

**Responsibilities:**
- Generate 6-character invite codes (7-day expiry)
- Validate and accept invite codes
- Create Pal links
- List user relationships

**Tech:** Spring Data JPA, UserRelationship entity

---

### Notification Service

**Responsibilities:**
- Create notifications (self or Pal)
- List sent/received notifications
- Get notification details
- Cancel pending notifications
- Mark notifications as viewed
- Update delivery status

**Tech:** Spring Data JPA, Notification entity

---

### Scheduler Service

**Responsibilities:**
- Poll the database every 10 seconds by default for due notifications
- Batch process up to 50 notifications per cycle
- Delegate to Push Service for delivery
- Handle delivery results (success/failure)
- Retry failed notifications once (60s delay)

**Tech:** Spring `@Scheduled`, Transaction management

---

### Push Service

**Responsibilities:**
- Send Web Push notifications via VAPID
- Handle multiple subscriptions per user
- Detect expired subscriptions (410 Gone)
- Revoke stale subscriptions while preserving explicit user removal
- Abstract provider interface for future extensibility

**Tech:** Web Push library (web-push-java), VAPID keys

---

### Frontend

**Responsibilities:**
- PWA shell (installable, offline-capable)
- Authentication UI (Google + magic link)
- Dashboard (Sent/Received tabs)
- Notification creation form
- Invite code management
- Push subscription registration
- Service worker for push handling

**Tech:** Next.js 16, React 19, TypeScript, Tailwind CSS, shadcn/ui, TanStack Query

---

### Database

**Responsibilities:**
- Store all persistent data
- Enforce constraints and relationships
- Support efficient queries for scheduler

**Tech:** PostgreSQL 15, managed by Railway

**Tables:** `users`, `user_relationships`, `push_subscriptions`, `notifications`,
`notification_deliveries`, `rate_limit_events`
