# PushPal — Review Checklist

Use this checklist when reviewing code.

---

## Review Items

### 1. Functionality

- [ ] Does the code do what it's supposed to do?
- [ ] Are all acceptance criteria met?
- [ ] Does it handle edge cases (empty state, null values)?
- [ ] Does it work on mobile and desktop?

### 2. Edge Cases

- [ ] What happens with empty input?
- [ ] What happens with very long input?
- [ ] What happens with invalid data?
- [ ] What happens when the user is not authenticated?
- [ ] What happens when the user is not authorized?
- [ ] What happens when the database is down?
- [ ] What happens when push delivery fails?

### 3. Security

- [ ] No secrets in code or logs
- [ ] Input is validated and sanitized
- [ ] User can only access their own data
- [ ] Rate limiting is applied where needed
- [ ] No SQL injection vectors
- [ ] No XSS vectors (React auto-escapes, but check dangerouslySetInnerHTML)

### 4. Performance

- [ ] No N+1 queries
- [ ] Database queries are indexed
- [ ] No unnecessary re-renders (React)
- [ ] No large bundle additions
- [ ] Pagination is implemented for lists

### 5. Push Delivery

- [ ] Push is tested on a real device
- [ ] Notification appears in the list
- [ ] Status updates correctly (PENDING → SENT → DELIVERED)
- [ ] Expired subscriptions are cleaned up
- [ ] Failed pushes are retried once

### 6. Error Messages

- [ ] Error messages are helpful to the user
- [ ] Error messages don't leak internal details
- [ ] Toast notifications are shown for errors
- [ ] Loading states are shown during async operations

### 7. Mobile

- [ ] Works on 375px viewport (iPhone SE)
- [ ] Touch targets are at least 44x44px
- [ ] No horizontal scrolling
- [ ] Bottom navigation is accessible
- [ ] Forms are usable on mobile keyboards

### 8. Logging

- [ ] No notification content in logs
- [ ] No user PII in logs
- [ ] Error logging includes stack trace
- [ ] Log levels are appropriate (not debug in prod)

### 9. Migrations

- [ ] Migration is backward-compatible
- [ ] Migration is idempotent (safe to re-run)
- [ ] No data loss (or documented)
- [ ] Indexes are added for new queries

### 10. Configuration

- [ ] No hardcoded values (use env vars)
- [ ] New env vars are documented
- [ ] Default values are sensible
- [ ] Feature flags are used for experimental features

---

## Common Issues to Watch For

| Issue | Where to Look |
|---|---|
| N+1 queries | Service layer, repository calls in loops |
| Missing loading states | Components that fetch data |
| Missing error handling | API calls, form submissions |
| Hardcoded URLs | Frontend API calls |
| Missing validation | Controller request bodies |
| Logging secrets | Error handlers, debug logs |
| Race conditions | Concurrent updates |
| Memory leaks | useEffect cleanup, subscriptions |
