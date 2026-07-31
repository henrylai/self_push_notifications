# PushPal — Error Handling

## Error Handling Strategy

All errors follow a consistent pattern: log the error (without sensitive data), return a standard error response, and handle gracefully on the client.

---

## Backend Error Responses

### Standard Error Format

```json
{
  "error": {
    "code": "NOTIFICATION_NOT_FOUND",
    "message": "Notification not found",
    "timestamp": "2025-01-15T10:30:00Z"
  }
}
```

### HTTP Status Codes

| Status | When | Example |
|---|---|---|
| 200 | Success | GET /notifications |
| 201 | Created | POST /notifications |
| 400 | Validation error | Missing required field |
| 401 | Not authenticated | Missing/expired JWT |
| 403 | Not authorized | Accessing another user's data |
| 404 | Resource not found | Notification doesn't exist |
| 409 | Conflict | Already linked to someone |
| 410 | Gone | Push subscription expired |
| 429 | Rate limited | Too many requests |
| 500 | Server error | Unexpected failure |
| 503 | Service unavailable | Database down |

---

## Push Notification Errors

### Failure Handling

```
Push fails
    │
    ├── 410 Gone (expired subscription)
    │   └── Delete subscription from DB
    │       └── Notify user (optional)
    │
    ├── 429 Rate limited
    │   └── Retry after delay (exponential backoff)
    │
    ├── Network error
    │   └── Retry once after 60 seconds
    │       └── If still fails → mark as FAILED
    │
    └── Unknown error
        └── Log error (no content)
            └── Mark as FAILED
```

### Retry Policy

| Attempt | Delay | Action |
|---|---|---|
| 1st failure | 60 seconds | Retry |
| 2nd failure | — | Mark as FAILED |

**Maximum retries:** 1 (two attempts total)

### Subscription Cleanup

When a push subscription returns 410 (Gone):
1. Delete the subscription from `push_subscriptions`
2. If user has no remaining subscriptions, optionally notify via email (V2)
3. Log the cleanup (no subscription details)

---

## Authentication Errors

### JWT Expiration

```
API receives request with expired JWT
    │
    ├── Return 401 { "error": "TOKEN_EXPIRED" }
    │
    └── Frontend intercepts 401
        └── Clear JWT from localStorage
            └── Redirect to /auth/login
```

### Invalid JWT

```
API receives request with invalid JWT
    │
    └── Return 401 { "error": "INVALID_TOKEN" }
        └── Frontend redirects to login
```

---

## Database Errors

### Connection Failure

```
Database unreachable
    │
    ├── Return 503 { "error": "SERVICE_UNAVAILABLE" }
    │
    └── Retry connection (Spring default: 3 attempts)
        └── If still down → 503 to client
```

### Constraint Violation

```
Unique constraint violated
    │
    └── Return 409 { "error": "CONFLICT", "message": "Already exists" }
```

---

## Rate Limiting

### Limits

| Endpoint | Limit | Window |
|---|---|---|
| POST /auth/magic-link | 3 | 1 hour |
| POST /notifications | 30 | 1 hour |
| POST /relationships/invite | 5 | 1 hour |
| All other endpoints | 60 | 1 minute |

### Rate Limit Response

```json
{
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many requests. Try again in 60 seconds.",
    "retryAfter": 60
  }
}
```

HTTP Status: 429

---

## Frontend Error Handling

### Error Display

| Error Type | Display |
|---|---|
| Validation | Inline field errors |
| API error | Toast notification (3 seconds) |
| Network error | Toast + retry button |
| Auth error | Redirect to login |
| Push permission denied | Banner with instructions |

### Optimistic Updates

When user creates/cancels a notification:
1. Update UI immediately (optimistic)
2. Send request to API
3. On failure → revert UI + show error toast

---

## Logging Policy

### What We Log

- Request method, path, status code
- Error type and stack trace (no user data)
- Push delivery success/failure (notification ID only)
- Auth events (login, logout, token refresh)

### What We NEVER Log

- Notification content (title, body)
- Push subscription endpoints
- JWT tokens
- User email or name
- Invite codes

### Log Format

```
2025-01-15T10:30:00Z INFO  POST /api/notifications 201 45ms
2025-01-15T10:30:01Z ERROR Push delivery failed for notification abc-123: Subscription expired
2025-01-15T10:30:02Z INFO  Subscription deleted: sub-456
```
