# PushPal — Milestones

## Milestone 1: "Hello Push"

**Goal:** User can sign in, register device, schedule a self-reminder, and receive it.

**Target:** End of Week 2

### Issues

| # | Issue | Area | Priority |
|---|---|---|---|
| 1 | Project scaffolding (backend + frontend) | infra | critical |
| 2 | Database schema + Flyway migrations | db | critical |
| 3 | Google OAuth2 sign-in | auth | critical |
| 4 | Magic link sign-in | auth | high |
| 5 | Device registration endpoint | push | critical |
| 6 | Push subscription storage | push | critical |
| 7 | Service worker + push handler | frontend | critical |
| 8 | Self-reminder creation (API + UI) | backend/frontend | critical |
| 9 | Notification list view (Sent/Received) | frontend | high |

**Completion criteria:**
- [x] User can sign in with Google or magic link (code complete; requires Railway env vars to activate)
- [x] User can register a device for push notifications
- [x] User can schedule a self-reminder
- [x] User receives push notification at scheduled time (code complete; verify on Railway)
- [x] User can see notifications in a list

---

## Milestone 2: "Hello Partner"

**Goal:** Two users can link and send reminders to each other.

**Target:** End of Week 4

### Issues

| # | Issue | Area | Priority |
|---|---|---|---|
| 10 | Invite code generation | backend | high |
| 11 | Invite code acceptance | backend | high |
| 12 | Couple linking UI | frontend | high |
| 13 | Send to partner (API + UI) | backend/frontend | high |
| 14 | Delivery status tracking | backend | high |
| 15 | Cancel pending notification | backend/frontend | medium |
| 16 | Notification detail view | frontend | medium |
| 17 | Push delivery scheduler | backend | critical |
| 18 | Error handling + retry logic | backend | high |

**Completion criteria:**
- [x] User can generate an invite code
- [x] User can accept an invite code to link with partner
- [x] User can send a reminder to their partner
- [x] Partner receives the notification (verify on Railway)
- [x] Sender can see delivery status
- [x] Sender can cancel a pending notification

---

## Milestone 3: "Production Ready"

**Goal:** Error handling, multi-device, PWA polish, tests passing.

**Target:** End of Week 6

### Issues

| # | Issue | Area | Priority |
|---|---|---|---|
| 19 | Multi-device registration | push | medium |
| 20 | PWA manifest + install prompt | frontend | high |
| 21 | Empty states + loading states | frontend | medium |
| 22 | Error states + toast notifications | frontend | medium |
| 23 | Mobile responsiveness pass | frontend | high |
| 24 | Push failure handling + cleanup | backend | high |
| 25 | Rate limiting | security | medium |
| 26 | Unit tests (80%+ coverage) | quality | high |
| 27 | Integration tests | quality | high |
| 28 | Deploy to Railway | devops | high |

**Completion criteria:**
- [ ] Multi-device works
- [ ] PWA installs successfully
- [ ] All error states are handled
- [ ] Mobile experience is polished
- [ ] Tests pass in CI
- [ ] Deployed to production

---

## Milestone 4: "V1 Launch"

**Goal:** Real usage, bug fixes, user feedback.

**Target:** End of Month 3

### Issues

| # | Issue | Area | Priority |
|---|---|---|---|
| 29 | Beta testing with real users | quality | high |
| 30 | Bug fixes from beta | bug | high |
| 31 | Performance optimization | backend/frontend | medium |
| 32 | Monitoring and alerting | devops | high |
| 33 | Privacy policy + terms | docs | high |
| 34 | Landing page | frontend | medium |
| 35 | Onboarding flow polish | frontend | medium |
| 36 | Launch announcement | docs | medium |

**Completion criteria:**
- [ ] 10+ beta users have used the app
- [ ] Critical bugs fixed
- [ ] Performance meets targets (< 200ms API, < 30s push)
- [ ] Monitoring is in place
- [ ] Privacy policy is published
- [ ] Ready for public launch
