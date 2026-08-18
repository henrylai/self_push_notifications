# PushPal — Privacy Policy (Draft)

*Last updated: January 2025*

---

## What We Store

PushPal stores only the data necessary to provide the service:

| Data | Purpose | Retention |
|---|---|---|
| Email address | Authentication and account identification | Until account deletion |
| Display name | Display in the app | Until account deletion |
| Push subscription tokens | Deliver push notifications | Until unsubscribed or account deletion |
| Notification content (title, body) | Deliver scheduled reminders | Until account deletion |
| Invite codes | Link with Pals | Until used or expired (7 days) |
| Relationship data | Track Pal links | Until unlinked |

---

## What We DON'T Store

- Passwords (we use OAuth2 and magic links)
- Payment information
- Location data
- Device information beyond push tokens
- Browsing history
- Contact lists

---

## How We Use Your Data

1. **Authentication** — Your email is used to sign you in via Google or magic link
2. **Push notifications** — Your push subscription token is used to deliver scheduled reminders
3. **Pal linking** — Invite codes connect you with people you choose
4. **Service improvement** — We track aggregate metrics (notification count, delivery rate) without personal data

---

## Data Sharing

**We do not sell, rent, or share your personal data with third parties.**

The only third-party services we use:

| Service | Purpose | Data shared |
|---|---|---|
| Google OAuth2 | Authentication | None (we receive your email/name) |
| Push services (FCM/APNs) | Deliver push notifications | Push subscription token only |
| Railway | Hosting | All data (encrypted) |
| Railway | Application hosting | Application data required to operate the service |

---

## Your Rights

You have the right to:

1. **Access** — Request a copy of all data we hold about you
2. **Delete** — Delete your account and all associated data
3. **Export** — Export your notifications as JSON
4. **Unsubscribe** — Remove your push subscription at any time

### How to Exercise Your Rights

- **Delete or export account data:** Contact the service operator. Self-service deletion and export
  are not yet available in the app.
- **Unsubscribe:** Browser settings > Notifications > Remove PushPal

---

## Data Security

- **Encryption in transit:** All communication uses TLS 1.3
- **Encryption at rest:** Database is encrypted using AES-256
- **No content logging:** We never log notification content
- **Minimal data:** We collect only what is necessary
- **Access control:** Users can only access their own data

---

## Data Retention

| Data | Retention Period |
|---|---|
| Account data | Until deletion |
| Delivered notifications | Until account deletion |
| Push subscriptions | Active until unsubscribed; revocation record until account deletion |
| Invite codes | Until account deletion; unusable after 7 days |
| Logs | 30 days, no personal data |

---

## Children's Privacy

PushPal is not intended for users under 13. We do not knowingly collect data from children.

---

## Changes to This Policy

We may update this policy. Users will be notified of significant changes via email.

---

## Contact

For privacy-related questions: privacy@pushpal.app
