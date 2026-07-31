# PushPal — Authentication Design

## Authentication Methods

### 1. Google OAuth2

**Flow:** Authorization Code Grant

```
User clicks "Sign in with Google"
    |
    v
Redirect to Google OAuth consent screen
    |
    v
User approves access
    |
    v
Google redirects back with authorization code
    |
    v
Frontend sends code to POST /api/auth/google
    |
    v
Backend exchanges code for tokens with Google
    |
    v
Backend gets user info (email, name, picture)
    |
    v
Backend creates/updates user in database
    |
    v
Backend generates JWT
    |
    v
Frontend stores JWT in localStorage
```

**Configuration:**
```yaml
google:
  client-id: ${GOOGLE_CLIENT_ID}
  client-secret: ${GOOGLE_CLIENT_SECRET}
  redirect-uri: https://pushpal.app/auth/callback
  scope: email profile
```

### 2. Magic Link

**Flow:** Email-based passwordless auth

```
User enters email on login page
    |
    v
Frontend sends email to POST /api/auth/magic-link
    |
    v
Backend generates magic link token
    |
    v
Backend sends email with link:
  https://pushpal.app/auth/verify?token=<magic_token>
    |
    v
User clicks link in email
    |
    v
Frontend sends token to GET /api/auth/verify
    |
    v
Backend validates token (exists, not expired, not used)
    |
    v
Backend creates/updates user in database
    |
    v
Backend marks token as used
    |
    v
Backend generates JWT
    |
    v
Frontend stores JWT in localStorage
```

**Magic Link Token Properties:**
- Length: 64 characters (random)
- Expiry: 15 minutes
- Single-use
- Stored hashed in database

---

## JWT Token Structure

### Payload

```json
{
  "sub": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "email": "user@example.com",
  "name": "John Doe",
  "iat": 1705312800,
  "exp": 1705917600
}
```

| Claim | Description |
|---|---|
| `sub` | User ID (UUID) |
| `email` | User's email |
| `name` | User's display name |
| `iat` | Issued at (Unix timestamp) |
| `exp` | Expiration (Unix timestamp, 7 days from iat) |

### Token Properties

| Property | Value |
|---|---|
| Algorithm | HS256 (HMAC-SHA256) |
| Secret | 256-bit random key |
| Expiry | 7 days |
| Storage | localStorage (client) |
| Header | `Authorization: Bearer <token>` |

---

## Security Controls

### Token Validation

Every protected endpoint validates:
1. Token exists in Authorization header
2. Token is not expired
3. Token signature is valid
4. User exists in database

### Token Refresh

- No refresh tokens for MVP (7-day expiry is sufficient)
- User must re-authenticate after expiry
- Future: add refresh tokens for better UX

### Token Invalidation

- Logout: Clear JWT from localStorage (client-side only)
- Future: Token blacklist for server-side invalidation

---

## Password Policy

**PushPal has no passwords.** Authentication is via:
- Google OAuth2 (Google manages passwords)
- Magic links (email-based, no password)

---

## Session Management

| Aspect | Approach |
|---|---|
| Token type | JWT (stateless) |
| Storage | localStorage |
| Expiry | 7 days |
| Refresh | None (MVP) |
| Logout | Client-side clear |
| Concurrent sessions | Allowed (multiple devices) |

---

## User Registration

**Automatic.** First time a user signs in (via Google or magic link), an account is created automatically. No separate registration step.

### User Creation Flow

```java
public User findOrCreate(String email, String name, AuthProvider provider) {
    return userRepository.findByEmail(email)
        .map(existing -> {
            existing.setName(name);
            return userRepository.save(existing);
        })
        .orElseGet(() -> {
            User newUser = User.builder()
                .email(email)
                .name(name)
                .authProvider(provider)
                .build();
            return userRepository.save(newUser);
        });
}
```
