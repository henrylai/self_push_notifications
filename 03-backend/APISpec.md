# PushPal — API Specification

## Base URL

```
Production: https://api.pushpal.app
Development: http://localhost:8080
```

## Authentication

All endpoints (except auth) require a JWT token in the `Authorization` header:

```
Authorization: Bearer <jwt_token>
```

## Standard Response Format

### Success

```json
{
  "data": { ... }
}
```

### Error

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable message"
  }
}
```

---

## Auth Endpoints

### POST /api/auth/google

Sign in with Google OAuth2 authorization code.

**Request:**
```json
{
  "code": "google_auth_code",
  "redirectUri": "https://pushpal.app/auth/callback"
}
```

**Response (200):**
```json
{
  "data": {
    "token": "jwt_token",
    "user": {
      "id": "uuid",
      "email": "user@gmail.com",
      "name": "John Doe",
      "authProvider": "GOOGLE"
    }
  }
}
```

---

### POST /api/auth/magic-link

Request a magic link sent to email.

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response (200):**
```json
{
  "data": {
    "message": "Check your email for the login link"
  }
}
```

---

### GET /api/auth/verify?token=<magic_link_token>

Verify magic link and return JWT.

**Response (200):**
```json
{
  "data": {
    "token": "jwt_token",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "name": "User",
      "authProvider": "EMAIL"
    }
  }
}
```

**Error (400):**
```json
{
  "error": {
    "code": "INVALID_TOKEN",
    "message": "Magic link is invalid or expired"
  }
}
```

---

### POST /api/auth/logout

Invalidate current session (client-side: clear JWT).

**Response (200):**
```json
{
  "data": {
    "message": "Logged out"
  }
}
```

---

## User Endpoints

### GET /api/users/me

Get current user profile.

**Response (200):**
```json
{
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "John Doe",
    "authProvider": "GOOGLE",
    "createdAt": "2025-01-15T10:00:00Z"
  }
}
```

---

### PUT /api/users/me

Update current user profile.

**Request:**
```json
{
  "name": "John Updated"
}
```

**Response (200):**
```json
{
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "John Updated",
    "authProvider": "GOOGLE",
    "createdAt": "2025-01-15T10:00:00Z"
  }
}
```

---

## Relationship Endpoints

### POST /api/relationships/invite

Generate an invite code.

**Response (201):**
```json
{
  "data": {
    "id": "uuid",
    "inviteCode": "A3F8K2",
    "expiresAt": "2025-01-22T10:00:00Z",
    "status": "PENDING"
  }
}
```

---

### POST /api/relationships/accept

Accept an invite code and link with partner.

**Request:**
```json
{
  "inviteCode": "A3F8K2"
}
```

**Response (200):**
```json
{
  "data": {
    "id": "uuid",
    "partner": {
      "id": "uuid",
      "name": "Partner Name",
      "email": "partner@example.com"
    },
    "status": "ACTIVE",
    "createdAt": "2025-01-15T10:00:00Z"
  }
}
```

**Error (409):**
```json
{
  "error": {
    "code": "ALREADY_LINKED",
    "message": "You are already linked with a partner"
  }
}
```

---

### GET /api/relationships

List user's relationships.

**Response (200):**
```json
{
  "data": [
    {
      "id": "uuid",
      "partner": {
        "id": "uuid",
        "name": "Partner Name",
        "email": "partner@example.com"
      },
      "status": "ACTIVE",
      "createdAt": "2025-01-15T10:00:00Z"
    }
  ]
}
```

---

## Device Endpoints

### POST /api/devices

Register a push subscription.

**Request:**
```json
{
  "endpoint": "https://fcm.googleapis.com/...",
  "keys": {
    "p256dh": "BNcRdreALRFXTkOOUHK1EtK2wtaz5Ry4YfYCA_0QTpQtUbVlUls0VJXg7A8u-Ts1XHh2B6i_",
    "auth": "tBHItJI5svmSD641XozNcg"
  }
}
```

**Response (201):**
```json
{
  "data": {
    "id": "uuid",
    "createdAt": "2025-01-15T10:00:00Z"
  }
}
```

---

### DELETE /api/devices/:id

Remove a push subscription.

**Response (200):**
```json
{
  "data": {
    "message": "Device removed"
  }
}
```

---

### GET /api/devices

List user's registered devices.

**Response (200):**
```json
{
  "data": [
    {
      "id": "uuid",
      "createdAt": "2025-01-15T10:00:00Z"
    }
  ]
}
```

---

## Notification Endpoints

### POST /api/notifications

Create a new notification.

**Request:**
```json
{
  "recipientId": "self | partner-uuid",
  "title": "Take out the trash",
  "body": "Don't forget!",
  "scheduledTime": "2025-01-15T19:00:00Z"
}
```

**Response (201):**
```json
{
  "data": {
    "id": "uuid",
    "title": "Take out the trash",
    "body": "Don't forget!",
    "scheduledTime": "2025-01-15T19:00:00Z",
    "status": "PENDING",
    "createdAt": "2025-01-15T10:00:00Z"
  }
}
```

**Error (400):**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Scheduled time must be in the future"
  }
}
```

---

### GET /api/notifications

List notifications (sent and received).

**Query Parameters:**
- `tab`: `sent` | `received` (default: both)
- `page`: Page number (default: 1)
- `limit`: Items per page (default: 20)

**Response (200):**
```json
{
  "data": {
    "notifications": [
      {
        "id": "uuid",
        "title": "Take out the trash",
        "body": "Don't forget!",
        "scheduledTime": "2025-01-15T19:00:00Z",
        "status": "PENDING",
        "sender": {
          "id": "uuid",
          "name": "John"
        },
        "recipient": {
          "id": "uuid",
          "name": "Jane"
        },
        "createdAt": "2025-01-15T10:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 1
    }
  }
}
```

---

### GET /api/notifications/:id

Get notification details.

**Response (200):**
```json
{
  "data": {
    "id": "uuid",
    "title": "Take out the trash",
    "body": "Don't forget!",
    "scheduledTime": "2025-01-15T19:00:00Z",
    "status": "DELIVERED",
    "sender": {
      "id": "uuid",
      "name": "John"
    },
    "recipient": {
      "id": "uuid",
      "name": "Jane"
    },
    "createdAt": "2025-01-15T10:00:00Z",
    "updatedAt": "2025-01-15T19:00:05Z"
  }
}
```

---

### DELETE /api/notifications/:id

Cancel a pending notification.

**Response (200):**
```json
{
  "data": {
    "message": "Notification cancelled"
  }
}
```

**Error (400):**
```json
{
  "error": {
    "code": "NOT_CANCELLABLE",
    "message": "Notification has already been sent"
  }
}
```

---

### POST /api/notifications/:id/viewed

Mark a notification as viewed.

**Response (200):**
```json
{
  "data": {
    "message": "Notification marked as viewed"
  }
}
```

---

## Push Subscription Endpoint

### POST /api/push/subscribe

Subscribe to push notifications (same as POST /api/devices).

**Request:**
```json
{
  "endpoint": "https://fcm.googleapis.com/...",
  "keys": {
    "p256dh": "BNcRdreALRFXTkOOUHK1EtK2wtaz5Ry4YfYCA_0QTpQtUbVlUls0VJXg7A8u-Ts1XHh2B6i_",
    "auth": "tBHItJI5svmSD641XozNcg"
  }
}
```

**Response (201):**
```json
{
  "data": {
    "id": "uuid"
  }
}
```
