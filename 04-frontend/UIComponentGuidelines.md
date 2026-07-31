# PushPal — UI Component Guidelines

## Design System

### Component Library

- **Base:** shadcn/ui (built on Radix UI primitives)
- **Styling:** Tailwind CSS
- **Icons:** Lucide React
- **Animations:** CSS transitions + Tailwind

### Color Palette

```typescript
// tailwind.config.ts
const colors = {
  primary: {
    50: '#eff6ff',
    100: '#dbeafe',
    500: '#3b82f6',  // Blue
    600: '#2563eb',
    700: '#1d4ed8',
  },
  success: '#22c55e',  // Green
  warning: '#f59e0b',  // Amber
  danger: '#ef4444',   // Red
  muted: '#6b7280',    // Gray
}
```

### Typography

- **Font:** System font stack (`-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, ...`)
- **Headings:** font-semibold
- **Body:** font-normal, text-sm or text-base
- **Monospace:** For codes, timestamps

### Spacing

- Base unit: 4px (Tailwind default)
- Card padding: p-4 (16px)
- Section gap: gap-4 (16px)
- Page margin: mx-4 (16px) mobile, mx-auto max-w-lg desktop

---

## Layout

### Mobile-First

- Maximum width: 480px (centered on larger screens)
- Bottom navigation for primary actions
- Pull-to-refresh on notification lists

### Dashboard Layout

```
┌──────────────────────┐
│  Header              │
│  ┌────┬────┬────┐   │
│  │Logo│    │User│   │
│  └────┴────┴────┘   │
├──────────────────────┤
│  ┌──────────────────┐│
│  │  Tabs            ││
│  │  [Sent] [Received]││
│  └──────────────────┘│
│                      │
│  ┌──────────────────┐│
│  │  NotificationCard ││
│  └──────────────────┘│
│  ┌──────────────────┐│
│  │  NotificationCard ││
│  └──────────────────┘│
│                      │
├──────────────────────┤
│  BottomNav           │
│  ┌────┬────┬────┐   │
│  │Home│ +  │Gear│   │
│  └────┴────┴────┘   │
└──────────────────────┘
```

---

## Component States

### Every component must handle these states:

| State | Description |
|---|---|
| **Empty** | No data to display |
| **Loading** | Fetching data |
| **Error** | Something went wrong |
| **Success** | Data loaded / action completed |
| **Disabled** | Interactive element is disabled |

### Empty State

```tsx
<div className="flex flex-col items-center justify-center py-12 text-center">
  <BellOff className="h-12 w-12 text-muted mb-4" />
  <h3 className="text-lg font-semibold">No notifications yet</h3>
  <p className="text-sm text-muted mt-1">
    Tap + to schedule your first reminder
  </p>
</div>
```

### Loading State

```tsx
<div className="space-y-4">
  <Skeleton className="h-24 w-full" />
  <Skeleton className="h-24 w-full" />
  <Skeleton className="h-24 w-full" />
</div>
```

### Error State

```tsx
<div className="flex flex-col items-center justify-center py-12 text-center">
  <AlertCircle className="h-12 w-12 text-danger mb-4" />
  <h3 className="text-lg font-semibold">Something went wrong</h3>
  <p className="text-sm text-muted mt-1">{error.message}</p>
  <Button onClick={retry} className="mt-4">Try again</Button>
</div>
```

---

## Components

### NotificationCard

```tsx
// Components: sender/recipient name, title, body preview, time, status badge
// States: pending, sent, delivered, viewed, failed, cancelled
// Actions: tap to view, cancel (pending only)

<NotificationCard
  notification={{
    id: 'uuid',
    title: 'Take out the trash',
    body: 'Don\'t forget!',
    scheduledTime: '2025-01-15T19:00:00Z',
    status: 'PENDING',
    sender: { name: 'John' },
    recipient: { name: 'Jane' }
  }}
  tab="sent"
/>
```

### Status Badge

```tsx
// Color-coded badges for notification status
// PENDING: yellow
// SENT: blue
// DELIVERED: green
// VIEWED: gray
// FAILED: red
// CANCELLED: gray with strikethrough

<Badge variant={statusVariant[status]}>
  {status}
</Badge>
```

### NotificationForm

```tsx
// Fields: title (required), body (optional), recipient, scheduled time
// Validation: title required, time must be future
// Submit: optimistic update + API call
// Error: inline field errors + toast
```

### InviteCodeDisplay

```tsx
// Shows 6-character code in monospace
// Copy button (clipboard API)
// Expiry countdown
// Share button (Web Share API if available)
```

---

## Interactions

### Pull to Refresh

- Available on notification lists
- Triggers TanStack Query refetch
- Shows loading spinner during refresh

### Swipe to Cancel

- Available on pending notifications in "Sent" tab
- Swipe left reveals "Cancel" button
- Confirmation dialog before cancelling

### Optimistic Updates

When user creates/cancels a notification:
1. Update UI immediately
2. Send request to API
3. On success → confirm update
4. On failure → revert + show toast

---

## Accessibility

- All interactive elements must be keyboard-navigable
- Color is never the only indicator (always paired with text/icon)
- Minimum touch target: 44x44px
- Focus visible on all interactive elements
- Screen reader labels on all buttons and inputs

---

## Toast Notifications

```tsx
// Success: "Scheduled!" / "Sent to [Partner]!" / "Cancelled"
// Error: "Something went wrong" / "Network error" / "Permission denied"
// Duration: 3 seconds (success), 5 seconds (error)
// Position: bottom center (mobile), bottom right (desktop)
```
