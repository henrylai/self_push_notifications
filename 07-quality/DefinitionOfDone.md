# PushPal — Definition of Done

A feature or issue is "Done" when ALL of the following criteria are met.

---

## Checklist

| # | Criterion | Verified By |
|---|---|---|
| 1 | **Code compiles** — No build errors in backend or frontend | CI pipeline |
| 2 | **Unit tests pass** — All existing tests still pass | CI pipeline |
| 3 | **Unit tests written** — New code has corresponding unit tests | PR review |
| 4 | **Integration tests pass** — API contract tests pass | CI pipeline |
| 5 | **API contract** — Endpoints match the API spec in `03-backend/APISpec.md` | PR review |
| 6 | **UI states** — All states handled: empty, loading, error, success | PR review |
| 7 | **Push delivery tested** — Notifications deliver correctly on real device | Manual test |
| 8 | **No regressions** — Existing functionality not broken | CI + manual |
| 9 | **Code reviewed** — At least 1 approval from team member | GitHub PR |
| 10 | **Deployed** — Merged to main and deployed to staging/production | Railway/Vercel |

---

## Detailed Criteria

### 1. Code Compiles

- Backend: `./gradlew build` succeeds
- Frontend: `npm run build` succeeds
- No compilation warnings (or documented exceptions)

### 2. Unit Tests Pass

- Backend: `./gradlew test` — all tests green
- Frontend: `npm run test` — all tests green
- No skipped tests without justification

### 3. Unit Tests Written

- New services/controllers have corresponding test classes
- Edge cases are tested (null inputs, empty states, errors)
- Mock providers used for push-related tests

### 4. Integration Tests Pass

- API endpoints tested with real database (Testcontainers)
- Auth flow tested end-to-end
- Notification creation and delivery tested

### 5. API Contract

- Endpoint paths match `APISpec.md`
- Request/response shapes match documentation
- HTTP status codes are correct
- Error responses follow standard format

### 6. UI States

- Empty state shown when no data
- Loading state shown during fetches
- Error state shown on failures
- Success state shown after actions
- All states are accessible (keyboard, screen reader)

### 7. Push Delivery Tested

- Notification received on real device
- Notification appears in list with correct status
- Status updates (PENDING → SENT → DELIVERED)
- Cancel works before delivery

### 8. No Regressions

- All existing functionality still works
- No breaking changes to API
- No database migration issues
- No performance degradation

### 9. Code Reviewed

- At least 1 approval required
- All review comments addressed
- No unresolved conversations

### 10. Deployed

- Merged to `main` branch
- CI/CD pipeline passes
- Backend deployed to Railway
- Frontend deployed to Vercel
- Smoke test on production

---

## Exceptions

Some criteria may be waived for:
- **Hotfixes** — Deploy first, write tests after (within 24 hours)
- **Documentation-only changes** — No tests needed
- **Infrastructure changes** — Deploy tested locally first

All exceptions must be documented in the PR description.
