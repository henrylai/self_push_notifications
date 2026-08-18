# PushPal — System Architecture

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTS                                  │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Chrome       │  │ Safari iOS   │  │ Firefox      │          │
│  │ Android      │  │ PWA          │  │ Desktop      │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                 │                  │                   │
│         └─────────────────┼──────────────────┘                   │
│                           │ Web Push (VAPID)                     │
└───────────────────────────┼─────────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────────┐
│                     BACKEND (Railway)                             │
│                           │                                      │
│  ┌────────────────────────▼────────────────────────────┐        │
│  │              Spring Boot 3 API                       │        │
│  │              (Java 21, Port 8080)                     │        │
│  │                                                      │        │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │        │
│  │  │ Auth        │  │ User        │  │ Notification│ │        │
│  │  │ Service     │  │ Service     │  │ Service     │ │        │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │        │
│  │                                                      │        │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │        │
│  │  │ Scheduler   │  │ Push        │  │ Relationship│ │        │
│  │  │ Service     │  │ Service     │  │ Service     │ │        │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │        │
│  └────────────────────────────────────────────────────┘        │
│                           │                                      │
│  ┌────────────────────────▼────────────────────────────┐        │
│  │              PostgreSQL (Railway)                     │        │
│  │              users | relationships |                   │        │
│  │              subscriptions | notifications             │        │
│  └────────────────────────────────────────────────────┘        │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────────┐
│                     FRONTEND (Vercel)                             │
│                           │                                      │
│  ┌────────────────────────▼────────────────────────────┐        │
│  │              Next.js 16 (Static Export)               │        │
│  │              App Router | Tailwind CSS                 │        │
│  │              shadcn/ui | TanStack Query                │        │
│  └────────────────────────────────────────────────────┘        │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

## Component Breakdown

| Component | Technology | Responsibility |
|---|---|---|
| **API Gateway** | Spring Boot (embedded Tomcat) | HTTP routing, request validation, error handling |
| **Auth Service** | Spring Security + OAuth2 | Google OAuth2, magic link, JWT issuance/validation |
| **User Service** | Spring Boot | User CRUD, profile management |
| **Relationship Service** | Spring Boot | Invite codes, couple linking |
| **Notification Service** | Spring Boot | Notification CRUD, status tracking |
| **Scheduler Service** | Spring @Scheduled | Polls for due notifications, triggers push delivery |
| **Push Service** | Web Push (VAPID) | Sends push notifications via push provider |
| **Frontend** | Next.js 16 + React 19 | PWA, UI, service worker, push subscription management |
| **Database** | PostgreSQL | Persistent storage for all entities |

## Data Flow: Send Notification

```
1. User fills form in Next.js frontend
2. Frontend sends POST /api/notifications to Spring Boot
3. API validates request, creates Notification (status=PENDING)
4. Scheduler polls every 30s, finds due notifications
5. Scheduler calls Push Service with recipient's subscriptions
6. Push Service sends Web Push via VAPID
7. On success: status → SENT → DELIVERED (via subscription success)
8. On failure: retry once after 60s, then → FAILED
```

## Deployment Architecture

| Service | Hosting | Details |
|---|---|---|
| Backend API | Railway | Docker, port 8080, auto-deploy from main |
| Database | Railway | PostgreSQL 15, automatic backups |
| Frontend | Vercel | Static export, edge network, auto-deploy from main |
