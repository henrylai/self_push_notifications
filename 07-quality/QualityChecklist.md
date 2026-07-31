# PushPal — Quality Checklist

Use this checklist before merging any PR.

---

## Pre-Merge Checklist

### Backend

- [ ] `./gradlew test` passes
- [ ] `./gradlew build` succeeds
- [ ] New code has unit tests
- [ ] Integration tests pass
- [ ] Database migrations are backward-compatible
- [ ] No raw SQL queries (use JPA)
- [ ] No secrets in code

### Frontend

- [ ] `npm run lint` passes
- [ ] `npm run typecheck` passes
- [ ] `npm run build` succeeds
- [ ] New components have tests
- [ ] All UI states handled (empty/loading/error/success)
- [ ] No `console.log` statements
- [ ] No secrets in code

### API

- [ ] Endpoints match API spec (`03-backend/APISpec.md`)
- [ ] Request validation is implemented
- [ ] Error responses follow standard format
- [ ] HTTP status codes are correct
- [ ] No breaking changes (or documented in PR)

### Push Notifications

- [ ] Push tested on real device
- [ ] Notification appears in list
- [ ] Status updates correctly
- [ ] Cancel works before delivery
- [ ] Expired subscriptions are cleaned up

### Security

- [ ] No TODOs in code
- [ ] No hardcoded secrets
- [ ] No `System.out.println` in production code
- [ ] Input validation is present
- [ ] Rate limiting applied where needed

### Mobile / UX

- [ ] Works on mobile viewport (375px)
- [ ] Touch targets are at least 44x44px
- [ ] Loading states are shown
- [ ] Error messages are helpful
- [ ] Empty states are informative

### Logging

- [ ] No notification content in logs
- [ ] No user PII in logs
- [ ] Error logging includes context (not secrets)
- [ ] Log levels are appropriate

### Documentation

- [ ] README updated (if applicable)
- [ ] API spec updated (if endpoints changed)
- [ ] Migration documented (if schema changed)
- [ ] ADR created (if new decision)

---

## Quick Reference

| Command | What It Checks |
|---|---|
| `./gradlew test` | Backend unit tests |
| `./gradlew build` | Backend compilation + tests |
| `npm run lint` | Frontend code style |
| `npm run typecheck` | TypeScript type safety |
| `npm run build` | Frontend compilation |
| `npm run test` | Frontend unit tests |

---

## Common Issues

| Issue | Fix |
|---|---|
| `./gradlew test` fails | Check test output, fix failing test or code |
| `npm run lint` fails | Run `npm run lint -- --fix` to auto-fix |
| `npm run typecheck` fails | Fix TypeScript errors |
| Migration fails | Ensure migration is backward-compatible |
| Push not working | Check VAPID keys, subscription registration |
