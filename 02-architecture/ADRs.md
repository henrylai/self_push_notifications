# PushPal — Architecture Decision Records (ADRs)

## ADR-001: Spring Boot 3 + Java 21

**Status:** Accepted

**Context:** Need a backend framework for a REST API with scheduled tasks, database access, and OAuth2.

**Decision:** Use Spring Boot 3 with Java 21.

**Rationale:**
- Mature ecosystem with excellent library support
- Spring Security for OAuth2 and JWT
- Spring Data JPA for database access
- Java 21 virtual threads for efficient scheduling
- Strong community and documentation

**Alternatives considered:**
- Node.js/Express: Rejected — less structured for scheduled tasks
- Go: Rejected — faster but less ecosystem for OAuth/JWT
- Kotlin/Spring: Could be adopted later, but Java is the team's strength

---

## ADR-002: PostgreSQL

**Status:** Accepted

**Context:** Need a relational database for users, relationships, notifications, and push subscriptions.

**Decision:** Use PostgreSQL 15.

**Rationale:**
- ACID compliance for critical notification data
- JSON support for flexible metadata
- Full-text search if needed later
- Railway provides managed PostgreSQL
- Excellent Spring Data JPA support

**Alternatives considered:**
- MySQL: Rejected — fewer features, less flexible
- SQLite: Rejected — not suitable for production web apps
- MongoDB: Rejected — relational data fits better in SQL

---

## ADR-003: Web Push with VAPID

**Status:** Accepted

**Context:** Need to deliver push notifications to browsers without a native app.

**Decision:** Use Web Push API with VAPID (Voluntary Application Server Identification).

**Rationale:**
- Standard across all modern browsers
- No third-party dependency (no Firebase, no OneSignal)
- VAPID ensures only our server can send to our users
- Works on Chrome, Firefox, Safari (PWA)
- Free and open protocol

**Alternatives considered:**
- Firebase Cloud Messaging: Rejected — adds Google dependency
- OneSignal: Rejected — third-party, cost at scale
- WebSockets: Rejected — not push, requires active connection

---

## ADR-004: Next.js Static Export

**Status:** Accepted

**Context:** Need a frontend that works as a PWA with good performance.

**Decision:** Use Next.js 16 with App Router and static export (`output: 'export'`).

**Rationale:**
- Static export works on any CDN (Vercel, Cloudflare)
- No server needed — pure client-side app
- App Router provides clean file-based routing
- TypeScript support out of the box
- Good ecosystem for PWAs

**Alternatives considered:**
- React SPA (Vite): Rejected — Next.js routing is cleaner
- Next.js SSR: Rejected — no need for server rendering
- Remix: Rejected — overkill for a static PWA

---

## ADR-005: Railway Hosting

**Status:** Accepted

**Context:** Need hosting for backend (Spring Boot) and database (PostgreSQL).

**Decision:** Use Railway for both backend and database.

**Rationale:**
- Simple deployment from GitHub
- Managed PostgreSQL included
- Automatic SSL
- Environment variable management
- Reasonable pricing for MVP
- Easy horizontal scaling later

**Alternatives considered:**
- AWS: Rejected — too complex for MVP
- Heroku: Rejected — expensive, limited free tier
- Fly.io: Considered — Railway is simpler

---

## ADR-006: Spring @Scheduled (Not Quartz)

**Status:** Accepted

**Context:** Need to poll for due notifications and trigger push delivery.

**Decision:** Use Spring's built-in `@Scheduled` annotation.

**Rationale:**
- Zero additional dependency
- Sufficient for polling every 30 seconds
- Simple to understand and maintain
- Good enough for MVP scale (< 10K notifications/day)

**Alternatives considered:**
- Quartz Scheduler: Rejected — overkill for simple polling
- Temporal: Rejected — too complex for MVP
- Database polling with lock: Considered — @Scheduled is simpler

---

## ADR-007: Gradle Wrapper

**Status:** Accepted

**Context:** Need a build tool for the Spring Boot backend.

**Decision:** Use Gradle with the Gradle Wrapper.

**Rationale:**
- Gradle is the default for Spring Boot projects
- Wrapper ensures consistent builds across environments
- Kotlin DSL for build scripts (type-safe)
- Faster builds than Maven for most projects

**Alternatives considered:**
- Maven: Rejected — XML config is verbose
- No wrapper: Rejected — ensures reproducible builds

---

## ADR-008: JWT Tokens (Not Sessions)

**Status:** Accepted

**Context:** Need stateless authentication for the API.

**Decision:** Use JWT tokens stored in localStorage on the client.

**Rationale:**
- Stateless — no server-side session store needed
- Works with static export frontend
- Simple to implement with Spring Security
- 7-day expiry for convenience

**Alternatives considered:**
- Server-side sessions: Rejected — requires sticky sessions or Redis
- HttpOnly cookies: Considered — localStorage is simpler for PWA
- OAuth2 access tokens: Considered — JWT gives more control

---

## ADR-009: Abstract NotificationProvider

**Status:** Accepted

**Context:** Need to send push notifications via Web Push, but may add more providers later.

**Decision:** Use an abstract `NotificationProvider` interface with a `WebPushProvider` implementation.

**Rationale:**
- Clean separation of concerns
- Easy to add FCM, APNs, or email later
- Testable with mock providers
- Follows SOLID principles

**Alternatives considered:**
- Direct Web Push calls: Rejected — harder to extend
- Strategy pattern: Considered — interface is simpler

---

## ADR-010: PWA Over Native

**Status:** Accepted

**Context:** Need cross-platform push notifications without building native apps.

**Decision:** Build a PWA first, native apps later.

**Rationale:**
- Faster to market (weeks vs months)
- Single codebase for all platforms
- Web Push works on Chrome, Firefox, Safari
- Can install on home screen
- Prove value before investing in native

**Alternatives considered:**
- React Native: Rejected — too early, no proven need
- Flutter: Rejected — same reason
- Native iOS + Android: Rejected — V3 feature
