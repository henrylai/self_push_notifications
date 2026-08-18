# AGENTS.md — AI Coding Agent Instructions for PushPal

## Project Overview

PushPal is a scheduled push notification PWA built with:
- **Backend:** Spring Boot 3 + Java 21 + PostgreSQL
- **Frontend:** Next.js 16 + React 19 + TypeScript + Tailwind CSS
- **Notifications:** Web Push (VAPID protocol)
- **Deployment:** Railway

## Repository Structure

```
├── backend/            # Spring Boot API
│   ├── build.gradle.kts
│   ├── src/main/java/com/pushpal/
│   │   ├── config/     # Security, CORS, WebPush config
│   │   ├── auth/       # JWT, OAuth, magic link
│   │   ├── user/       # User entity, repo, service, controller
│   │   ├── relationship/ # Couple linking
│   │   ├── notification/ # Notification CRUD + scheduler
│   │   ├── device/     # Push subscription management
│   │   ├── push/       # Web Push provider abstraction
│   │   └── common/     # Error handling, shared DTOs
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/  # Flyway SQL migrations
├── frontend/           # Next.js PWA
│   ├── src/app/        # App Router pages
│   ├── src/components/ # React components
│   ├── src/lib/        # API client, auth, push, utilities
│   ├── src/hooks/      # React hooks (TanStack Query)
│   └── src/types/      # TypeScript type definitions
└── [docs dirs]/        # 00-vision through 09-project
```

## Development Commands

### Backend
```bash
cd backend
./gradlew bootRun          # Start dev server
./gradlew test             # Run tests
./gradlew build            # Build JAR
./gradlew flywayMigrate    # Run migrations
```

### Frontend
```bash
cd frontend
npm run dev                # Start dev server
npm run build              # Build for production
npm run lint               # Run ESLint
npm run typecheck          # TypeScript type checking
```

## Code Style

### Java (Backend)
- Follow Google Java Style Guide
- Use Lombok for boilerplate (`@Getter`, `@Setter`, `@Builder`)
- Use `record` types for DTOs where appropriate
- Prefer `Optional` over null checks
- Use `@NonNull` / `@Nullable` annotations
- All timestamps use `Instant` (UTC)
- All IDs use `UUID`
- Return DTOs from controllers, never entities directly

### TypeScript (Frontend)
- Strict mode enabled
- Prefer `const` over `let`
- Use interfaces for object shapes
- Avoid `any` type — use proper types
- Prefer named exports
- Use `clsx` + `tailwind-merge` for conditional classes

### Database
- Always create Flyway migrations — never modify existing ones
- Migration naming: `V{N}__description.sql`
- Index foreign keys and frequently queried columns
- Use `UUID` for primary keys
- Use `TIMESTAMPTZ` for all timestamp columns

## Architecture Rules

### Notification Flow
1. User creates notification → stored in DB with status `PENDING`
2. Scheduler polls every 30 seconds for `PENDING` notifications where `scheduled_time <= NOW()`
3. For each due notification, look up recipient's push subscriptions
4. Send Web Push to ALL recipient subscriptions via `NotificationProvider` interface
5. Update status to `SENT` (or `FAILED` on error)
6. Retry once after 60s on failure, then mark `FAILED`

### Notification Status Lifecycle
```
PENDING → SENT → DELIVERED → VIEWED
    ↓
  FAILED (after retry exhaustion)
    ↓
CANCELLED (user cancelled before send)
```

### Auth Flow
1. User authenticates via Google OAuth or Magic Link
2. Backend returns JWT (7-day expiry)
3. Frontend stores JWT in localStorage
4. All API requests include `Authorization: Bearer <JWT>`
5. `JwtAuthenticationFilter` validates on every request

### Push Subscription Flow
1. Service worker registers on page load
2. Browser requests push permission
3. `PushManager.subscribe()` with VAPID key
4. Subscription sent to `POST /api/devices/register`
5. Stored in `push_subscriptions` table
6. Used by scheduler to deliver notifications

## Key Abstractions

### NotificationProvider (Backend)
```java
public interface NotificationProvider {
    SendResult send(PushSubscription subscription, NotificationPayload payload);
}
```
All push delivery goes through this interface. `WebPushProvider` is the current implementation. Future: FCM, APNs, Email.

### API Client (Frontend)
All API calls go through `src/lib/api.ts`. Never make raw `fetch()` calls outside this file.

## Testing

### Backend
- Unit tests: Service layer with mocked repositories
- Integration tests: Controller → Service → Repository → H2 database
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` for API tests
- Use Testcontainers for PostgreSQL integration tests (production-like)

### Frontend
- Unit tests: Utility functions
- Component tests: React Testing Library (key flows only)
- E2E tests: Playwright (auth + notification creation)

## Environment Variables

See `backend/src/main/resources/application.yml` for all backend config.
See `frontend/.env.example` for frontend config.

## Common Tasks

### Add a new API endpoint
1. Create/update entity in the domain package
2. Create/update repository interface
3. Create/update service class
4. Create/update controller with proper annotations
5. Add Flyway migration if schema changed
6. Update API tests

### Add a new frontend page
1. Create page in `src/app/(dashboard)/` (authenticated) or `src/app/(auth)/` (public)
2. Add route to navigation if needed
3. Create any new components in `src/components/`
4. Add API methods to `src/lib/api.ts`
5. Add TanStack Query hooks if needed
6. Add types to `src/types/index.ts`

### Add a new database column
1. Create Flyway migration: `V{N}__add_column_to_table.sql`
2. Update entity class
3. Update any affected DTOs
4. Run `./gradlew flywayMigrate` to verify
5. Update tests

## Security Rules

- Never commit secrets (JWT_SECRET, VAPID keys, etc.)
- Never log notification content in production
- Never expose internal IDs or errors in API responses
- All API endpoints (except auth) require JWT
- Rate limit: 10 reminders/hour per user
- Only linked users can send notifications to each other

## Documentation

Product docs are in the numbered directories (`00-vision/` through `09-project/`). Read them for context on why decisions were made.

Start here:
- `08-ai/AgentInstructions.md` — Detailed agent instructions
- `08-ai/CodingStandards.md` — Full coding standards
- `08-ai/ReviewChecklist.md` — Pre-merge checklist
- `03-backend/APISpec.md` — Full API specification
- `03-backend/DatabaseSchema.md` — Database DDL
