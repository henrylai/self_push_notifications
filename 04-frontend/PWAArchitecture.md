# PushPal — PWA Architecture

## Tech Stack

- **Framework:** Next.js 16 + React 19 (App Router)
- **Rendering:** Static Export (`output: 'export'`)
- **Styling:** Tailwind CSS
- **Components:** shadcn/ui
- **State:** TanStack Query (server state), React Context (auth)
- **Language:** TypeScript (strict mode)
- **PWA:** Service Worker + Web App Manifest

---

## Directory Structure

```
frontend/
├── app/
│   ├── layout.tsx              # Root layout (providers, metadata)
│   ├── page.tsx                # Landing / redirect to dashboard
│   ├── auth/
│   │   ├── page.tsx            # Login page
│   │   ├── callback/
│   │   │   └── page.tsx        # OAuth callback handler
│   │   └── magic-link/
│   │       └── page.tsx        # Magic link verification
│   ├── dashboard/
│   │   ├── page.tsx            # Main dashboard (Sent/Received tabs)
│   │   ├── new/
│   │   │   └── page.tsx        # Create notification
│   │   └── [id]/
│   │       └── page.tsx        # Notification detail
│   └── settings/
│       ├── page.tsx            # Settings page
│       └── settings/page.tsx   # Profile, Pal linking, and devices
├── components/
│   ├── ui/                     # shadcn/ui components
│   │   ├── button.tsx
│   │   ├── input.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── toast.tsx
│   │   ├── tabs.tsx
│   │   ├── badge.tsx
│   │   └── skeleton.tsx
│   ├── cards/
│   │   ├── NotificationCard.tsx
│   │   └── PalCard.tsx
│   ├── forms/
│   │   ├── NotificationForm.tsx
│   │   ├── LoginForm.tsx
│   │   └── InviteCodeForm.tsx
│   └── layout/
│       ├── Header.tsx
│       ├── BottomNav.tsx
│       └── EmptyState.tsx
├── lib/
│   ├── api.ts                  # API client (fetch wrapper)
│   ├── auth.ts                 # Auth utilities (JWT, login/logout)
│   ├── push.ts                 # Push subscription management
│   └── pwa.ts                  # PWA utilities (install prompt)
├── hooks/
│   ├── useAuth.ts              # Auth context hook
│   ├── useNotifications.ts     # TanStack Query hooks for notifications
│   ├── useRelationship.ts      # TanStack Query hooks for relationships
│   └── usePush.ts              # Push subscription hook
├── types/
│   ├── index.ts                # Shared types
│   ├── notification.ts         # Notification types
│   ├── user.ts                 # User types
│   └── api.ts                  # API response types
├── public/
│   ├── icons/
│   │   ├── icon-192.png
│   │   ├── icon-512.png
│   │   └── badge-72.png
│   ├── manifest.json
│   └── sw.js                   # Service worker (generated)
├── next.config.js
├── tailwind.config.ts
├── tsconfig.json
├── package.json
└── Dockerfile
```

---

## Page Architecture

### Auth Pages (`/auth`)

```
/auth
├── page.tsx              # Login with Google + Magic Link
├── callback/page.tsx     # Handles OAuth redirect
└── magic-link/page.tsx   # Verifies magic link token
```

### Dashboard Pages (`/dashboard`)

```
/dashboard
├── page.tsx              # Sent/Received tabs + notification list
├── new/page.tsx          # Create notification form
└── [id]/page.tsx         # Notification detail + status
```

### Settings Pages (`/settings`)

```
/settings
└── page.tsx              # Profile, Pal linking, and devices
```

---

## Component Hierarchy

```
RootLayout
├── AuthProvider (Context)
├── QueryProvider (TanStack Query)
├── Toaster (shadcn/ui)
└── {children}
    └── DashboardLayout
        ├── Header
        │   └── Logo + User avatar
        ├── Main content
        │   └── {children}
        └── BottomNav
            ├── Dashboard
            ├── New Notification
            └── Settings
```

---

## Data Flow

```
Component
    │
    ▼
useNotifications() hook
    │
    ▼
TanStack Query
    │
    ▼
api.ts (fetch wrapper)
    │
    ▼
Spring Boot API
    │
    ▼
Response → Cache → Component re-render
```

---

## PWA Features

| Feature | Implementation |
|---|---|
| Installable | Web App Manifest + install prompt |
| Offline support | Service worker caching (static assets) |
| Push notifications | Service worker push event handler |
| Add to home screen | Auto-prompt on supported browsers |
| Standalone display | `display: standalone` in manifest |

---

## Routing Strategy

| Route | Auth Required | Description |
|---|---|---|
| `/` | No | Redirect to dashboard or login |
| `/auth` | No | Login page |
| `/auth/callback` | No | OAuth callback |
| `/auth/magic-link` | No | Magic link verification |
| `/dashboard` | Yes | Main dashboard |
| `/dashboard/new` | Yes | Create notification |
| `/dashboard/[id]` | Yes | Notification detail |
| `/settings` | Yes | User settings |
| `/dashboard/settings` | Yes | Profile and Pal linking |
