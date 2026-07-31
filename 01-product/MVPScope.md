# PushPal — MVP Scope

## What's In (V1)

| Feature | Details |
|---|---|
| **Authentication** | Google OAuth2 + Magic Link |
| **Device Registration** | Web Push subscription registration |
| **Self-Reminders** | Schedule a push notification for yourself |
| **Couple Linking** | Invite code system (6-char alphanumeric, 7-day expiry) |
| **Send to Partner** | Schedule a push notification for your linked partner |
| **Delivery Status** | Sent → Delivered → Viewed tracking |
| **Cancel Pending** | Cancel a notification before it's sent |
| **PWA Installable** | Add to home screen on mobile and desktop |

## What's NOT In (V2+)

| Feature | Version |
|---|---|
| Recurring reminders | V2 |
| Snooze / dismiss actions | V2 |
| Groups (3+ people) | V2 |
| Read receipts (per-message) | V2 |
| Multi-device | V2 |
| Email / SMS fallback | V2 |
| Timezone handling | V2 |
| Dark mode | V2 |
| i18n | V2 |
| Native apps | V3 |

## MVP User Limit

- **1 user** = self-reminders only
- **2 users** = self + one linked partner
- No groups, no teams, no multi-partner

## MVP Platform Support

| Platform | Support Level |
|---|---|
| Chrome Android | Full |
| Chrome Desktop | Full |
| Safari iOS (PWA) | Full |
| Firefox | Best effort |
| Edge | Best effort |

## Success Criteria for MVP

- [ ] User can sign in and register a device in < 30 seconds
- [ ] User can schedule a self-reminder in < 5 seconds
- [ ] User can send a reminder to partner in < 10 seconds
- [ ] Push notifications arrive within 30 seconds of scheduled time
- [ ] > 99% delivery rate
- [ ] PWA installs successfully on mobile and desktop
