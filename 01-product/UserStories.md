# PushPal — User Stories

## US-001: Google Sign-In

**As a** user,
**I want to** sign in with my Google account,
**So that** I can access PushPal quickly without creating a new password.

### Acceptance Criteria
- [ ] "Sign in with Google" button is visible on the login page
- [ ] Clicking the button redirects to Google OAuth consent screen
- [ ] On successful auth, user is redirected to the dashboard
- [ ] JWT token is stored in localStorage
- [ ] User profile (name, email, avatar) is saved to the database

---

## US-002: Magic Link Sign-In

**As a** user,
**I want to** sign in with a magic link sent to my email,
**So that** I can access PushPal without a Google account.

### Acceptance Criteria
- [ ] User can enter their email on the login page
- [ ] A magic link is sent to the provided email
- [ ] Clicking the magic link authenticates the user
- [ ] Magic link expires after 15 minutes
- [ ] Magic link is single-use

---

## US-003: Device Registration

**As a** user who has signed in,
**I want to** register my device for push notifications,
**So that** I can receive scheduled reminders.

### Acceptance Criteria
- [ ] Browser prompts for notification permission on first visit
- [ ] On grant, push subscription is sent to the backend
- [ ] Subscription is stored with user ID, endpoint, and keys
- [ ] If permission is denied, user sees a helpful message
- [ ] Re-registration updates the existing subscription

---

## US-004: Create Self-Reminder

**As a** user,
**I want to** schedule a reminder for myself,
**So that** I get notified at a specific time.

### Acceptance Criteria
- [ ] User can tap a "+" button to create a notification
- [ ] User can enter a title (required, max 100 chars)
- [ ] User can enter a body (optional, max 500 chars)
- [ ] User can pick a date and time (must be in the future)
- [ ] On submit, notification is saved with status PENDING
- [ ] User sees confirmation toast
- [ ] Core loop (tap → type → pick time → submit) takes < 5 seconds

---

## US-005: Generate Invite Code

**As a** user,
**I want to** generate an invite code,
**So that** I can link with a Pal.

### Acceptance Criteria
- [ ] User can access invite code from settings/dashboard
- [ ] Code is a 6-character alphanumeric string
- [ ] Code expires after 7 days
- [ ] Code can be shared via copy-to-clipboard
- [ ] Each user can have one active code at a time

---

## US-006: Accept Invite Code

**As a** user with an invite code,
**I want to** enter a Pal's code,
**So that** we can send reminders to each other.

### Acceptance Criteria
- [ ] User can enter an invite code on the dashboard
- [ ] Code is validated (exists, not expired, not self)
- [ ] On acceptance, both users are linked
- [ ] Relationship status becomes ACTIVE
- [ ] Error shown for invalid/expired/self codes

---

## US-007: Send Reminder to a Pal

**As a** linked user,
**I want to** send a scheduled reminder to a selected Pal,
**So that** they get notified at a specific time.

### Acceptance Criteria
- [ ] Every linked Pal is available when creating a notification
- [ ] Selected Pal's name/avatar is shown as recipient
- [ ] Same fields as self-reminder (title, body, time)
- [ ] Notification is saved with sender and recipient IDs
- [ ] Recipient receives push notification at scheduled time
- [ ] Sender sees notification in "Sent" tab

---

## US-008: View Notifications

**As a** user,
**I want to** see all my sent and received notifications,
**So that** I can track what's been sent and what's coming.

### Acceptance Criteria
- [ ] Dashboard shows two tabs: "Sent" and "Received"
- [ ] Each notification shows title, body, time, and status
- [ ] Status badges: Pending, Sent, Delivered, Viewed, Failed, Cancelled
- [ ] Notifications are sorted by scheduled time (newest first)
- [ ] Empty state is shown when no notifications exist

---

## US-009: Cancel Notification

**As a** user,
**I want to** cancel a pending notification,
**So that** it's not delivered if I change my mind.

### Acceptance Criteria
- [ ] Cancel button is visible on pending notifications
- [ ] Clicking cancel shows confirmation dialog
- [ ] On confirm, status changes to CANCELLED
- [ ] Cancelled notifications are not delivered
- [ ] Cancelled notifications still appear in the list (greyed out)

---

## US-010: View Delivery Status

**As a** sender,
**I want to** know the delivery status of my notification,
**So that** I can trust it was received.

### Acceptance Criteria
- [ ] Status shown on each sent notification
- [ ] Status updates in real-time (or near real-time)
- [ ] Status flow: PENDING → SENT → DELIVERED → VIEWED
- [ ] FAILED status shown with retry indicator
- [ ] Timestamp for each status change is visible
