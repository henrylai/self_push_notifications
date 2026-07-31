# PushPal — Roadmap

## Phase 1: MVP (Months 1–3)

### Sprint 1: Foundation (Weeks 1–2)

| Issue | Priority | Area |
|---|---|---|
| Project scaffolding (backend + frontend) | Critical | Infra |
| Database schema + Flyway migrations | Critical | DB |
| Google OAuth2 sign-in | Critical | Auth |
| Magic link sign-in | High | Auth |
| Device registration endpoint | Critical | Push |
| Push subscription storage | Critical | Push |
| Service worker + push handler | Critical | Frontend |
| Self-reminder creation (API + UI) | Critical | Core |
| Notification list view (Sent/Received) | High | Core |

**Milestone:** "Hello Push" — User can sign in, register device, schedule a self-reminder, and receive it.

---

### Sprint 2: Partner Features (Weeks 3–4)

| Issue | Priority | Area |
|---|---|---|
| Invite code generation | High | Core |
| Invite code acceptance | High | Core |
| Couple linking UI | High | Core |
| Send to partner (API + UI) | High | Core |
| Delivery status tracking | High | Core |
| Cancel pending notification | Medium | Core |
| Notification detail view | Medium | Frontend |
| Push delivery scheduler | Critical | Backend |
| Error handling + retry logic | High | Backend |

**Milestone:** "Hello Partner" — Two users can link and send reminders to each other.

---

### Sprint 3: Polish (Weeks 5–6)

| Issue | Priority | Area |
|---|---|---|
| Multi-device registration | Medium | Push |
| PWA manifest + install prompt | High | Frontend |
| Empty states + loading states | Medium | Frontend |
| Error states + toast notifications | Medium | Frontend |
| Mobile responsiveness pass | High | Frontend |
| Push failure handling + cleanup | High | Backend |
| Rate limiting | Medium | Security |
| Unit tests (80%+ coverage) | High | Quality |
| Integration tests | High | Quality |
| Deploy to Railway | High | DevOps |

**Milestone:** "Production Ready" — Error handling, multi-device, PWA polish, tests passing.

---

## Phase 2: V2 (Months 4–6)

| Feature | Priority |
|---|---|
| Recurring reminders | High |
| Snooze / dismiss actions | High |
| Groups (3+ people) | Medium |
| Read receipts | Medium |
| Email / SMS fallback | Medium |
| Quiet hours | Medium |
| Timezone handling | Medium |
| Dark mode | Low |
| i18n | Low |

---

## Phase 3: V3 (Months 7–9)

| Feature | Priority |
|---|---|
| iOS native app (Swift) | High |
| Android native app (Kotlin) | High |
| Location-based triggers | Medium |
| Calendar sync | Medium |
| Rich notifications (images) | Medium |

---

## Phase 4: V4 (Months 10–12)

| Feature | Priority |
|---|---|
| Team features | High |
| Admin dashboard | Medium |
| Analytics | Medium |
| Monetization (subscriptions) | High |
| API for third-party integrations | Low |
