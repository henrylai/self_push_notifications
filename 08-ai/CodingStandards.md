# PushPal — Coding Standards

## General

| Standard | Value |
|---|---|
| Max line length | 120 characters |
| Indentation (Java) | 4 spaces |
| Indentation (TypeScript) | 2 spaces |
| Indentation (HTML/CSS) | 2 spaces |
| Trailing commas | Yes (TypeScript) |
| Semicolons | Yes (TypeScript) |

---

## Java (Backend)

### Style Guide

Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with these additions:

```java
// Records for DTOs
public record NotificationResponse(
    UUID id,
    String title,
    String body,
    Instant scheduledTime,
    NotificationStatus status,
    Instant createdAt
) {}

// Service example
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RelationshipService relationshipService;

    public NotificationResponse create(
        CreateNotificationRequest request,
        User currentUser
    ) {
        // Implementation
    }
}

// Repository example
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientIdAndStatusOrderByScheduledTimeDesc(
        UUID recipientId,
        NotificationStatus status
    );
}
```

### Rules

- Use **records** for DTOs (not @Data classes)
- Use **Optional** for nullable returns
- Use **@RequiredArgsConstructor** for dependency injection
- Use **@Transactional** on service methods that modify data
- Use **Instant** for all timestamps (not Date, not LocalDateTime)
- Use **UUID** for all IDs (not Long, not String)
- Use **@Builder** only when constructor has 5+ parameters
- No `System.out.println` — use SLF4J logger
- No magic numbers — use constants

### Naming

| Type | Convention | Example |
|---|---|---|
| Classes | PascalCase | `NotificationService` |
| Methods | camelCase | `findByRecipientId` |
| Variables | camelCase | `notificationId` |
| Constants | UPPER_SNAKE | `MAX_TITLE_LENGTH` |
| Packages | lowercase | `com.pushpal.notification` |

---

## TypeScript (Frontend)

### Style Guide

```typescript
// Strict TypeScript
'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { Notification, NotificationStatus } from '@/types';

// Named exports only
export function NotificationCard({ notification }: { notification: Notification }) {
  // const, not let
  const statusVariant = getStatusVariant(notification.status);

  return (
    <div className="rounded-lg border p-4">
      <h3 className="font-semibold">{notification.title}</h3>
      <Badge variant={statusVariant}>{notification.status}</Badge>
    </div>
  );
}

// Helper outside component
function getStatusVariant(status: NotificationStatus) {
  switch (status) {
    case 'PENDING': return 'warning';
    case 'SENT': return 'info';
    case 'DELIVERED': return 'success';
    case 'VIEWED': return 'secondary';
    case 'FAILED': return 'destructive';
    case 'CANCELLED': return 'outline';
  }
}
```

### Rules

- **Strict TypeScript** — no `any` types
- Use **const** over **let** (always)
- Use **named exports** (not default exports)
- Use **arrow functions** for components
- Use **interface** for props (not type)
- Use **type** for unions and primitives
- No `console.log` in production code
- No inline styles — use Tailwind classes
- No magic strings — use constants or enums

### Naming

| Type | Convention | Example |
|---|---|---|
| Components | PascalCase | `NotificationCard` |
| Hooks | camelCase (use prefix) | `useNotifications` |
| Functions | camelCase | `getStatusVariant` |
| Types/Interfaces | PascalCase | `NotificationResponse` |
| Constants | UPPER_SNAKE | `API_BASE_URL` |
| Files (components) | PascalCase | `NotificationCard.tsx` |
| Files (utils) | camelCase | `api.ts` |
| CSS classes | kebab-case (Tailwind) | `rounded-lg` |

---

## Git Commit Messages

### Format

```
<type>(<scope>): <description>

[optional body]
```

### Types

| Type | When to Use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `chore` | Maintenance, dependencies, config |
| `docs` | Documentation only |
| `test` | Adding or fixing tests |
| `refactor` | Code change that neither fixes a bug nor adds a feature |

### Examples

```
feat(notification): add cancel endpoint
fix(push): handle expired subscriptions
chore(backend): update Spring Boot to 3.2
docs(api): update notification endpoints
test(service): add NotificationService unit tests
refactor(auth): extract JwtService from AuthService
```

### Rules

- Lowercase
- Imperative mood ("add" not "added")
- No period at end
- Reference issues: `feat(auth): add Google OAuth (#12)`
