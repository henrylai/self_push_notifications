# PushPal — Epic Breakdown

## Overview

| Epic | Issues | Description |
|---|---|---|
| Auth | 8 | Authentication and user management |
| Self-Reminders | 10 | Core notification functionality |
| Couple Linking | 6 | Partner relationship features |
| Multi-Device | 4 | Multiple device support |
| Polish | 8 | UX, testing, deployment |
| **Total** | **36** | |

---

## Epic 1: Auth (8 issues)

| # | Issue | Priority | Area |
|---|---|---|---|
| 1 | Project scaffolding | critical | infra |
| 2 | Database schema + migrations | critical | db |
| 3 | Google OAuth2 sign-in | critical | auth |
| 4 | Magic link sign-in | high | auth |
| 5 | JWT token service | critical | auth |
| 6 | Auth middleware/filter | critical | auth |
| 7 | User profile endpoint | medium | backend |
| 8 | Auth error handling | high | backend |

**Definition of Done:**
- User can sign in with Google
- User can sign in with magic link
- JWT is issued and validated
- Protected routes require authentication

---

## Epic 2: Self-Reminders (10 issues)

| # | Issue | Priority | Area |
|---|---|---|---|
| 9 | Device registration endpoint | critical | push |
| 10 | Push subscription storage | critical | push |
| 11 | Service worker + push handler | critical | frontend |
| 12 | Notification creation (API) | critical | backend |
| 13 | Notification creation (UI) | critical | frontend |
| 14 | Notification list (API) | high | backend |
| 15 | Notification list (UI) | high | frontend |
| 16 | Push delivery scheduler | critical | backend |
| 17 | Delivery status tracking | high | backend |
| 18 | Cancel pending notification | medium | backend/frontend |

**Definition of Done:**
- User can register a device
- User can schedule a self-reminder
- User receives push notification at scheduled time
- User can see notifications in a list
- User can cancel pending notifications

---

## Epic 3: Couple Linking (6 issues)

| # | Issue | Priority | Area |
|---|---|---|---|
| 19 | Invite code generation | high | backend |
| 20 | Invite code acceptance | high | backend |
| 21 | Couple linking UI | high | frontend |
| 22 | Send to partner (API) | high | backend |
| 23 | Send to partner (UI) | high | frontend |
| 24 | Notification detail view | medium | frontend |

**Definition of Done:**
- User can generate an invite code
- User can accept an invite code
- Two users are linked as partners
- User can send a reminder to their partner
- Partner receives the notification

---

## Epic 4: Multi-Device (4 issues)

| # | Issue | Priority | Area |
|---|---|---|---|
| 25 | Multiple subscription support | medium | backend |
| 26 | Device list endpoint | medium | backend |
| 27 | Device management UI | medium | frontend |
| 28 | Subscription cleanup | high | backend |

**Definition of Done:**
- User can register multiple devices
- Notifications are sent to all devices
- Expired subscriptions are cleaned up
- User can see and manage registered devices

---

## Epic 5: Polish (8 issues)

| # | Issue | Priority | Area |
|---|---|---|---|
| 29 | PWA manifest + install prompt | high | frontend |
| 30 | Empty states + loading states | medium | frontend |
| 31 | Error states + toast notifications | medium | frontend |
| 32 | Mobile responsiveness pass | high | frontend |
| 33 | Rate limiting | medium | security |
| 34 | Unit tests (80%+ coverage) | high | quality |
| 35 | Integration tests | high | quality |
| 36 | Deploy to Railway + Vercel | high | devops |

**Definition of Done:**
- PWA installs on mobile and desktop
- All UI states are handled
- Mobile experience is polished
- Tests pass in CI
- Deployed to production
