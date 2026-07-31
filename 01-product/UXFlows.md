# PushPal — User Experience Flows

## Flow 1: Onboarding

```
User opens PushPal URL
    │
    ▼
Login Page
    │
    ├──► "Sign in with Google" ──► Google OAuth ──► Dashboard
    │
    └──► "Sign in with email" ──► Enter email ──► Check email ──► Click magic link ──► Dashboard
                                                                              │
                                                                              ▼
                                                                    Notification Permission Prompt
                                                                    ├── Allow ──► Device registered ──► Dashboard
                                                                    └── Deny ──► Dashboard (limited)
```

**Key decisions:**
- Auth is the first screen — no landing page for MVP
- Device registration happens immediately after first auth
- Denying notifications doesn't block access (user can still send)

---

## Flow 2: Self-Reminder

```
Dashboard
    │
    ▼
Tap "+" FAB (floating action button)
    │
    ▼
New Notification Sheet
    │
    ├── Title (required, 100 chars max)
    ├── Body (optional, 500 chars max)
    ├── Recipient: "Me" (default, pre-selected)
    └── Scheduled Time (date picker + time picker)
    │
    ▼
Tap "Schedule"
    │
    ├── Validation fails ──► Show inline errors
    │
    └── Validation passes ──► Submit to API
                              │
                              ├── Success ──► Toast "Scheduled!" ──► Notification appears in "Sent" tab
                              └── Failure ──► Toast "Something went wrong"
```

**Target:** < 5 seconds from tap to submit.

---

## Flow 3: Send to Partner

```
Dashboard
    │
    ▼
Tap "+" FAB
    │
    ▼
New Notification Sheet
    │
    ├── Title (required)
    ├── Body (optional)
    ├── Recipient: Tap to change ──► Select Partner
    │     └── Shows partner name/avatar
    └── Scheduled Time
    │
    ▼
Tap "Send"
    │
    ├── No partner linked ──► "Link a partner first" ──► Invite flow
    │
    └── Partner linked ──► Submit to API
                           │
                           ├── Success ──► Toast "Sent to [Partner]!" ──► Appears in "Sent" tab
                           └── Failure ──► Toast "Failed to send"
```

**Target:** < 10 seconds from tap to submit.

---

## Flow 4: Receive Notification

```
Phone receives push notification
    │
    ▼
Notification banner appears
    │
    ├── Tap notification ──► Opens PushPal PWA
    │     │
    │     ▼
    │   Notification Detail Card
    │     ├── From: [Sender name]
    │     ├── Title: [title]
    │     ├── Body: [body]
    │     ├── Scheduled: [time]
    │     └── Status: Delivered
    │     │
    │     ▼
    │   Mark as "Viewed" (automatic)
    │
    └── Swipe away ──► Notification stays in "Received" tab as unread
```

---

## Flow 5: Invite / Couple Linking

```
Dashboard
    │
    ▼
Settings or "Link Partner" prompt
    │
    ├──► I have a code
    │     │
    │     ▼
    │   Enter invite code
    │     │
    │     ├── Invalid ──► Error "Invalid or expired code"
    │     └── Valid ──► Confirm ──► Linked! ──► Dashboard
    │
    └──► Share my code
          │
          ▼
        Show invite code + "Copy" button
          │
          ▼
        Share via message (manual)
          │
          ▼
        Partner enters code (see above)
```

**Invite code format:** 6 alphanumeric characters (e.g., `A3F8K2`).
**Expiry:** 7 days.
**Limit:** One active code per user at a time.
