# PushPal — Testing Strategy

## Testing Pyramid

```
         ┌─────┐
         │ E2E │  5%  (Playwright)
         ├─────┤
         │ INT │  25% (Testcontainers + API)
         ├─────┤
         │ UNIT│  70% (JUnit + Mockito + RTL)
         └─────┘
```

---

## Backend Testing

### Unit Tests (70%)

| Component | Framework | Coverage Target |
|---|---|---|
| AuthService | JUnit 5 + Mockito | 90% |
| JwtService | JUnit 5 + Mockito | 95% |
| MagicLinkService | JUnit 5 + Mockito | 90% |
| UserService | JUnit 5 + Mockito | 85% |
| RelationshipService | JUnit 5 + Mockito | 85% |
| NotificationService | JUnit 5 + Mockito | 85% |
| DeviceService | JUnit 5 + Mockito | 85% |
| PushService | JUnit 5 + Mockito | 80% |
| SchedulerService | JUnit 5 + Mockito | 80% |

### Example Unit Test

```java
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RelationshipService relationshipService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotification_selfReminder_success() {
        // Arrange
        User currentUser = TestFixtures.user();
        CreateNotificationRequest request = new CreateNotificationRequest(
            "self", "Take out the trash", null, Instant.now().plusHours(1)
        );

        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        NotificationResponse response = notificationService.create(request, currentUser);

        // Assert
        assertNotNull(response);
        assertEquals("Take out the trash", response.title());
        assertEquals(NotificationStatus.PENDING, response.status());
        assertNull(response.senderId());
        assertEquals(currentUser.getId(), response.recipientId());
    }
}
```

### Integration Tests (25%)

| Test | Framework | What It Tests |
|---|---|---|
| Auth flow | Testcontainers + MockMvc | Google OAuth + magic link end-to-end |
| Notification CRUD | Testcontainers + MockMvc | Create, read, cancel, view |
| Push delivery | Testcontainers + MockPushProvider | Scheduler → push → status update |
| Couple linking | Testcontainers + MockMvc | Invite → accept → send |

### Example Integration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAndReceiveNotification() {
        // 1. Create user
        // 2. Register device
        // 3. Create notification
        // 4. Verify notification exists
        // 5. Trigger scheduler
        // 6. Verify push was sent
        // 7. Mark as viewed
        // 8. Verify status updated
    }
}
```

### Push Testing

| Approach | When |
|---|---|
| Mock provider | Unit/integration tests (CI) |
| Real device | Manual testing (before release) |
| Staging server | Pre-production verification |

---

## Frontend Testing

### Unit Tests (70%)

| Component | Framework | Coverage Target |
|---|---|---|
| NotificationCard | React Testing Library | 90% |
| NotificationForm | React Testing Library | 85% |
| LoginForm | React Testing Library | 85% |
| InviteCodeForm | React Testing Library | 85% |
| StatusBadge | React Testing Library | 95% |

### Example Unit Test

```typescript
import { render, screen } from '@testing-library/react';
import { NotificationCard } from '@/components/cards/NotificationCard';

describe('NotificationCard', () => {
  it('renders notification title and status', () => {
    const notification = {
      id: '1',
      title: 'Take out the trash',
      status: 'PENDING',
      scheduledTime: '2025-01-15T19:00:00Z',
    };

    render(<NotificationCard notification={notification} tab="sent" />);

    expect(screen.getByText('Take out the trash')).toBeInTheDocument();
    expect(screen.getByText('Pending')).toBeInTheDocument();
  });
});
```

### E2E Tests (5%)

| Flow | Framework |
|---|---|
| Login → Dashboard | Playwright |
| Create notification | Playwright |
| Partner linking | Playwright |
| Push notification receive | Playwright + browser notifications |

### Example E2E Test

```typescript
import { test, expect } from '@playwright/test';

test('user can create a self-reminder', async ({ page }) => {
  // Login
  await page.goto('/auth');
  await page.click('text=Sign in with Google');
  // ... complete OAuth flow

  // Navigate to create
  await page.click('[data-testid="create-notification"]');

  // Fill form
  await page.fill('input[name="title"]', 'Test reminder');
  await page.fill('input[name="scheduledTime"]', '2025-01-15T19:00');

  // Submit
  await page.click('button[type="submit"]');

  // Verify
  await expect(page.locator('text=Scheduled!')).toBeVisible();
});
```

---

## Test Configuration

### Backend

```kotlin
// build.gradle.kts
tasks.test {
    useJUnitPlatform()
    maxParallelForks = 2
}
```

### Frontend

```json
// package.json
{
  "scripts": {
    "test": "jest",
    "test:watch": "jest --watch",
    "test:e2e": "playwright test"
  }
}
```

---

## Coverage Goals

| Layer | Target | Minimum |
|---|---|---|
| Backend unit | 80% | 70% |
| Backend integration | 75% | 60% |
| Frontend unit | 80% | 70% |
| E2E | Key flows | Login, create, partner |

---

## CI Integration

All tests run on every PR:
1. Backend tests (JUnit + Testcontainers)
2. Frontend tests (Jest)
3. Frontend E2E (Playwright) — nightly or on demand
