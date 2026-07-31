# PushPal — Implementation Order

## Week 1: Foundation

### Dependencies: None

| Order | Issue | Area | Hours | Depends On |
|---|---|---|---|---|
| 1 | Project scaffolding (backend + frontend) | infra | 4 | None |
| 2 | Database schema + Flyway migrations | db | 3 | #1 |
| 3 | Spring Boot config + CORS + error handling | backend | 4 | #1 |
| 4 | Google OAuth2 sign-in (backend) | auth | 6 | #2, #3 |
| 5 | Auth middleware (JWT filter) | auth | 3 | #4 |
| 6 | User profile endpoints | backend | 2 | #5 |

**End of Week 1:** Backend runs, Google OAuth works, user can sign in and get profile.

---

## Week 2: Core Push + Self-Reminders

### Dependencies: Week 1

| Order | Issue | Area | Hours | Depends On |
|---|---|---|---|---|
| 7 | Push subscription endpoint + storage | push | 4 | #5 |
| 8 | Notification creation endpoint | backend | 4 | #5 |
| 9 | Magic link sign-in (backend) | auth | 4 | #2, #3 |
| 10 | Frontend auth pages (login, callback) | frontend | 6 | #4, #9 |
| 11 | Service worker + push handler | frontend | 4 | #7 |
| 12 | Frontend dashboard + notification list | frontend | 6 | #10, #8 |
| 13 | Notification creation form (UI) | frontend | 4 | #12 |
| 14 | Push delivery scheduler | backend | 6 | #8, #7 |

**End of Week 2:** User can sign in, register device, schedule self-reminder, receive push. **Milestone 1: "Hello Push"**

---

## Week 3: Partner Features

### Dependencies: Week 2

| Order | Issue | Area | Hours | Depends On |
|---|---|---|---|---|
| 15 | Invite code generation (backend) | backend | 3 | #5 |
| 16 | Invite code acceptance (backend) | backend | 4 | #15 |
| 17 | Send to partner endpoint | backend | 4 | #16, #8 |
| 18 | Delivery status tracking | backend | 3 | #14 |
| 19 | Cancel pending notification | backend | 2 | #8 |
| 20 | Couple linking UI | frontend | 6 | #15, #16 |
| 21 | Send to partner UI | frontend | 4 | #17, #20 |
| 22 | Notification detail view | frontend | 3 | #12, #18 |

**End of Week 3:** Two users can link, send reminders to each other, see delivery status.

---

## Week 4: Polish + Multi-Device

### Dependencies: Week 3

| Order | Issue | Area | Hours | Depends On |
|---|---|---|---|---|
| 23 | Multi-device registration | backend | 4 | #7 |
| 24 | Subscription cleanup (410 handling) | backend | 3 | #14 |
| 25 | Error handling + retry logic | backend | 4 | #14 |
| 26 | Rate limiting | security | 3 | #3 |
| 27 | PWA manifest + install prompt | frontend | 3 | #12 |
| 28 | Empty states + loading states | frontend | 3 | #12 |
| 29 | Mobile responsiveness pass | frontend | 4 | #12, #13, #20, #21 |
| 30 | Toast notifications | frontend | 2 | #12 |

**End of Week 4:** Multi-device works, error handling complete, PWA installs. **Milestone 2: "Hello Partner"**

---

## Week 5: Testing

### Dependencies: Week 4

| Order | Issue | Area | Hours | Depends On |
|---|---|---|---|---|
| 31 | Unit tests - Auth | quality | 4 | #4, #5, #9 |
| 32 | Unit tests - Notification | quality | 4 | #8, #14, #17, #19 |
| 33 | Unit tests - Relationship | quality | 3 | #15, #16 |
| 34 | Unit tests - Push | quality | 3 | #7, #14, #24 |
| 35 | Integration tests | quality | 6 | All backend |
| 36 | Frontend tests | quality | 4 | All frontend |

**End of Week 5:** 80%+ test coverage, all tests passing.

---

## Week 6: Deploy + Launch Prep

### Dependencies: Week 5

| Order | Issue | Area | Hours | Depends On |
|---|---|---|---|---|
| 37 | Deploy backend to Railway | devops | 3 | #31-36 |
| 38 | Deploy frontend to Vercel | devops | 2 | #36 |
| 39 | CI/CD pipeline | devops | 4 | #37, #38 |
| 40 | Monitoring + alerting | devops | 3 | #37 |
| 41 | Privacy policy | docs | 2 | None |
| 42 | Beta testing | quality | 8 | #37, #38 |
| 43 | Bug fixes from beta | bug | 8 | #42 |

**End of Week 6:** Production deployment, monitoring in place, beta feedback incorporated. **Milestone 3: "Production Ready"**

---

## Summary

| Week | Focus | Milestone |
|---|---|---|
| Week 1 | Foundation (auth, DB, config) | — |
| Week 2 | Core push + self-reminders | Hello Push |
| Week 3 | Partner features | — |
| Week 4 | Polish + multi-device | Hello Partner |
| Week 5 | Testing | — |
| Week 6 | Deploy + launch prep | Production Ready |

**Total estimated hours:** ~150 hours (6 weeks full-time)
