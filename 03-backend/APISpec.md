# PushPal — API Specification

## Base URL

```text
Production: configured per deployment
Development: http://localhost:8080
```

All endpoints except authentication and the scoped delivery callback require a JWT:

```http
Authorization: Bearer <jwt_token>
```

Successful responses contain the documented JSON directly. Errors use this shape:

```json
{
  "id": "error-correlation-uuid",
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable message",
  "timestamp": "2026-08-10T12:00:00Z"
}
```

## Authentication

### POST `/api/auth/google`

Exchange a Google OAuth authorization code for a PushPal JWT.

```json
{
  "code": "google_authorization_code",
  "redirectUri": "https://pushpal.app/auth/callback"
}
```

```json
{
  "token": "jwt_token",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "User Name"
  }
}
```

### POST `/api/auth/magic-link`

Request a single-use magic link. The response remains generic to prevent account discovery.

```json
{
  "email": "user@example.com"
}
```

```json
{
  "message": "If an account exists with user@example.com, a magic link has been sent."
}
```

### POST `/api/auth/magic-link/verify`

Consume a magic-link token and return the same authentication response as Google login.
Magic links are single-use and expire after 15 minutes. Requests are limited to five per email
address in a rolling hour.

```json
{
  "token": "<magic_link_token>"
}
```

### POST `/api/auth/logout`

Acknowledges logout. JWT removal is client-side.

```json
{
  "message": "Logged out successfully"
}
```

## Users

### GET `/api/users/me`

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "name": "User Name"
}
```

### PUT `/api/users/me`

```json
{
  "name": "Updated Name"
}
```

Returns the updated user object.

## Relationships

### POST `/api/relationships/invite`

```json
{
  "inviteCode": "A3F8K2"
}
```

### POST `/api/relationships/accept`

```json
{
  "inviteCode": "A3F8K2"
}
```

```json
{
  "message": "Invite accepted successfully"
}
```

### GET `/api/relationships`

```json
[
  {
    "id": "uuid",
    "palId": "uuid",
    "palName": "Pal Name",
    "palEmail": "pal@example.com",
    "status": "ACCEPTED"
  }
]
```

Pending invitations have null Pal fields and status `PENDING`. The legacy `partnerId`,
`partnerName`, and `partnerEmail` fields are returned during the client migration period.

### DELETE `/api/relationships/{id}`

Either linked user may remove a Pal. Pending reminders in either direction are cancelled; sent,
delivered, viewed, and failed reminders remain in history.

```json
{
  "message": "Pal removed and pending reminders cancelled"
}
```

## Devices

Push subscription secrets are accepted during registration but are never returned by list APIs.

### POST `/api/devices/register`

```json
{
  "endpoint": "https://fcm.googleapis.com/fcm/send/subscription",
  "p256dh": "browser_public_key",
  "auth": "browser_auth_secret",
  "userAgent": "Browser user agent",
  "reactivate": true
}
```

```json
{
  "message": "Subscription registered successfully",
  "deviceId": "uuid"
}
```

Only HTTPS endpoints belonging to supported browser push services are accepted. `reactivate`
must only be true after an explicit user action to enable notifications; background synchronization
cannot resurrect a device that the user removed.

### POST `/api/devices/unregister`

Durably revoke the current browser subscription before locally unsubscribing it.

```json
{
  "endpoint": "https://fcm.googleapis.com/fcm/send/subscription"
}
```

### DELETE `/api/devices/{id}`

Only the owning user can remove a device. Removal revokes the record so background registration
cannot silently restore it.

```json
{
  "message": "Subscription removed"
}
```

### GET `/api/devices`

```json
[
  {
    "id": "uuid",
    "userAgent": "Browser user agent",
    "createdAt": "2026-08-10T12:00:00Z",
    "lastUsedAt": "2026-08-10T12:00:00Z"
  }
]
```

## Notifications

Users may schedule reminders for themselves or any accepted linked Pal. Requests are limited to
10 reminders per sender in a rolling hour.

### POST `/api/notifications`

```json
{
  "recipientId": "linked-pal-uuid",
  "title": "Take out the trash",
  "body": "Don't forget!",
  "icon": "calendar",
  "scheduledTime": "2026-08-10T19:00:00Z"
}
```

`recipientId` may be omitted for a self-reminder. `body` is optional. `title` is limited to 100
characters, `body` to 500 characters, and `scheduledTime` must be in the future. `icon` is
optional and defaults to `bell`; supported values are `bell`, `heart`, `star`, `check`,
`calendar`, and `gift`.

Returns a notification object:

```json
{
  "id": "uuid",
  "senderId": "uuid",
  "recipientId": "uuid",
  "senderName": "Sender Name",
  "recipientName": "Recipient Name",
  "title": "Take out the trash",
  "body": "Don't forget!",
  "icon": "calendar",
  "scheduledTime": "2026-08-10T19:00:00Z",
  "status": "PENDING",
  "createdAt": "2026-08-10T12:00:00Z"
}
```

### GET `/api/notifications?page=0&size=50`

Returns notifications scoped to the authenticated user. `page` starts at zero and `size` is limited
to 1–100. Self-reminders appear only in `sent`.

```json
{
  "received": [],
  "sent": [],
  "page": 0,
  "size": 50,
  "receivedHasMore": false,
  "sentHasMore": false
}
```

### GET `/api/notifications/{id}`

Returns a notification only when the authenticated user is its sender or recipient. Other IDs are
reported as not found.

### DELETE `/api/notifications/{id}`

The sender may cancel a notification while it is `PENDING`.

```json
{
  "message": "Notification cancelled"
}
```

### POST `/api/notifications/{id}/viewed`

The recipient may move a `SENT` or `DELIVERED` notification to `VIEWED`. Returns the updated
notification.

### POST `/api/notifications/{id}/delivered`

Internal service-worker callback. It does not accept an account JWT. The push payload supplies a
short-lived token scoped to one notification:

```http
X-PushPal-Delivery-Token: <scoped_delivery_token>
```

Returns the updated notification. Missing, expired, or mismatched delivery tokens receive `403`.
