# PushPal — AI Agent Instructions

Instructions for AI agents (Copilot, Cursor, Claude, etc.) working on the PushPal codebase.

---

## Project Overview

PushPal is a scheduled push notification PWA for couples and families. Spring Boot backend + Next.js frontend + PostgreSQL.

---

## Backend (Spring Boot 3 + Java 21)

### Conventions

- Use **Spring Boot 3** with **Java 21**
- All IDs are **UUIDs** (not auto-increment)
- All timestamps are **UTC** (Instant)
- Use **records** for DTOs (not classes)
- Use **Lombok** sparingly (@Data, @Builder)
- Use **Optional** for nullable returns
- Use **Spring Data JPA** for database access
- Use **Flyway** for migrations (never modify existing migrations)

### Package Structure

```
com.pushpal
├── config/       # Security, CORS, WebPush config
├── auth/         # Authentication (JWT, Google, Magic Link)
├── user/         # User CRUD
├── relationship/ # Couple linking
├── notification/ # Notification CRUD + status
├── device/       # Push subscription management
├── push/         # Push delivery (NotificationProvider)
├── scheduler/    # Scheduled tasks
└── common/       # Shared utilities, exception handler
```

### Code Style

- 4-space indentation
- Google Java Style Guide
- Records for DTOs: `record UserResponse(UUID id, String email, String name) {}`
- Service layer handles business logic
- Controller layer handles HTTP
- Repository layer handles data access

### Testing

- Unit tests: JUnit 5 + Mockito
- Integration tests: Testcontainers + MockMvc
- Push tests: MockNotificationProvider

---

## Frontend (Next.js 14 + TypeScript)

### Conventions

- **Next.js 14** with **App Router**
- **Static export** (`output: 'export'`)
- **TypeScript** in strict mode
- **Tailwind CSS** for styling
- **shadcn/ui** for components
- **TanStack Query** for server state
- **React Context** for auth only

### Directory Structure

```
app/          # Pages (App Router)
components/   # Reusable components
  ui/         # shadcn/ui primitives
  cards/      # NotificationCard, PartnerCard
  forms/      # NotificationForm, LoginForm
  layout/     # Header, BottomNav, EmptyState
lib/          # Utilities (api, auth, push, pwa)
hooks/        # Custom hooks
types/        # TypeScript types
```

### Code Style

- 2-space indentation
- `const` over `let`
- Named exports (not default exports)
- Components: PascalCase files
- Utilities: camelCase files
- Types: PascalCase in `types/` directory

### State Management

- **TanStack Query** for all server data (notifications, users, relationships)
- **React Context** for auth state only (JWT, user)
- **useState** for local UI state
- **React Hook Form** for forms
- No Redux, Zustand, or other global state libraries

### Testing

- Unit tests: React Testing Library
- E2E tests: Playwright
- Test files: `*.test.tsx` or `*.spec.tsx`

---

## Database

### Conventions

- **PostgreSQL 15**
- **UUIDs** for all primary keys
- **Timestamps** in UTC
- **Flyway** for migrations
- Never modify existing migrations

### Tables

- `users` — User accounts
- `user_relationships` — Couple links
- `push_subscriptions` — Push notification subscriptions
- `notifications` — Scheduled notifications

---

## Push Notifications

### Conventions

- Use **NotificationProvider** interface
- Start with **WebPushProvider** (VAPID)
- Never log notification content
- Handle expired subscriptions (410 → delete)
- Retry failed notifications once (60s delay)

---

## Security

### Rules

- **Never commit secrets** to Git
- **Never log secrets** or user data
- **JWT** for authentication (7-day expiry)
- **VAPID** for push authentication
- **Rate limiting** on all endpoints
- **Input validation** on all endpoints

---

## Git

### Commit Messages

- Lowercase imperative: "add notification creation endpoint"
- No period at the end
- Reference issue numbers when applicable

### Branch Naming

- `feature/short-description`
- `fix/short-description`
- `chore/short-description`
