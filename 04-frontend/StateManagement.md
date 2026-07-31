# PushPal — State Management

## Philosophy

PushPal uses a minimal state management approach:

- **Server state** → TanStack Query (fetching, caching, mutations)
- **Auth state** → React Context (JWT, user info)
- **UI state** → Local component state (useState)
- **Form state** → React Hook Form (form handling)
- **No global state library** (Redux, Zustand, Jotai)

---

## State Categories

### 1. Server State (TanStack Query)

All data from the API is managed by TanStack Query. This includes:

- User profile
- Notifications (sent/received)
- Relationships
- Devices

#### Query Hooks

```typescript
// hooks/useNotifications.ts

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { Notification, CreateNotificationRequest } from '@/types';

export function useNotifications(tab: 'sent' | 'received') {
  return useQuery({
    queryKey: ['notifications', tab],
    queryFn: () => api.get(`/notifications?tab=${tab}`),
  });
}

export function useNotification(id: string) {
  return useQuery({
    queryKey: ['notification', id],
    queryFn: () => api.get(`/notifications/${id}`),
  });
}

export function useCreateNotification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateNotificationRequest) =>
      api.post('/notifications', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
}

export function useCancelNotification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => api.delete(`/notifications/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
}

export function useMarkAsViewed() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => api.post(`/notifications/${id}/viewed`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
}
```

#### Cache Strategy

```typescript
// lib/api.ts (TanStack Query Provider config)

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,      // 30 seconds
      gcTime: 5 * 60_000,     // 5 minutes
      retry: 2,
      refetchOnWindowFocus: true,
    },
  },
});
```

---

### 2. Auth State (React Context)

```typescript
// contexts/AuthContext.tsx

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

interface AuthContextType extends AuthState {
  login: (token: string, user: User) => void;
  logout: () => void;
  updateUser: (user: User) => void;
}
```

#### Storage

- **JWT:** localStorage (`pushpal_token`)
- **User:** localStorage (`pushpal_user`)
- **On mount:** Check localStorage, validate token with API

#### Auth Flow

```
App mount
    │
    ▼
Check localStorage for token
    │
    ├── No token → Show login page
    │
    └── Token found → Validate with API
        │
        ├── Invalid → Clear storage → Show login
        │
        └── Valid → Set user in context → Show dashboard
```

---

### 3. UI State (Local State)

All UI state is managed with `useState` in components:

```typescript
// Component-level state examples

const [activeTab, setActiveTab] = useState<'sent' | 'received'>('received');
const [isFormOpen, setIsFormOpen] = useState(false);
const [searchQuery, setSearchQuery] = useState('');
const [selectedNotification, setSelectedNotification] = useState<string | null>(null);
```

---

### 4. Form State (React Hook Form)

```typescript
// components/forms/NotificationForm.tsx

import { useForm } from 'react-hook-form';

const form = useForm<CreateNotificationRequest>({
  defaultValues: {
    title: '',
    body: '',
    recipientId: 'self',
    scheduledTime: '',
  },
  mode: 'onBlur',
});

// Validation
const schema = z.object({
  title: z.string().min(1).max(100),
  body: z.string().max(500).optional(),
  recipientId: z.string(),
  scheduledTime: z.string().refine((val) => new Date(val) > new Date()),
});
```

---

## Data Flow Diagram

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Component  │────►│ TanStack     │────►│ API (fetch) │
│   (React)    │◄────│ Query Cache  │◄────│             │
└─────────────┘     └──────────────┘     └─────────────┘
       │
       │
       ▼
┌──────────────┐
│ Auth Context │
│ (JWT, User)  │
└──────────────┘
```

---

## What We Don't Need

| State Library | Why Not |
|---|---|
| Redux | Too heavy for this app's complexity |
| Zustand | TanStack Query handles server state |
| Jotai | No complex derived state needed |
| MobX | No complex reactive state needed |
| Recoil | No atom-based state needed |

---

## Persistence

| Data | Storage | Duration |
|---|---|---|
| JWT token | localStorage | Until logout |
| User profile | localStorage | Until logout |
| Push subscription | Browser (service worker) | Until unsubscribed |
| Notification drafts | Not persisted | Lost on refresh |
| UI preferences | Not persisted | Reset on refresh |

---

## Offline Behavior

- TanStack Query serves cached data when offline
- Mutations fail gracefully (show error toast)
- Push subscription is managed by service worker (works offline)
- No offline-first write operations (MVP)
