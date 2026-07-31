# PushPal — Scheduler Design

## Overview

The Scheduler Service polls the database every 30 seconds for notifications that are due and triggers push delivery.

---

## Polling Flow

```
Every 30 seconds (@Scheduled fixedRate = 30000)
    │
    ▼
┌─────────────────────────────────┐
│  1. Query PENDING notifications │
│     WHERE scheduled_time <= NOW()│
│     ORDER BY scheduled_time ASC  │
│     LIMIT 50                     │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│  2. For each notification:       │
│     a. Look up recipient's       │
│        push subscriptions        │
│     b. Send Web Push to each     │
│     c. Update status based on    │
│        result                    │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│  3. Handle results:              │
│     - Success → SENT             │
│     - Expired sub → DELETE sub   │
│     - Failure → Retry once       │
│       (60s delay)                │
│     - Retry fails → FAILED       │
└─────────────────────────────────┘
```

---

## Batch Processing

### Why Batch?

- Prevents loading thousands of notifications into memory
- Allows other schedulers to pick up work (distributed-safe)
- Limits blast radius if something goes wrong

### Batch Size

**50 notifications per cycle** — chosen because:
- At 30s intervals, 50 notifications = ~100/minute throughput
- Sufficient for MVP (expected < 1K notifications/day)
- Each notification may hit 1-2 subscriptions
- Total push sends per cycle: ~50-100

### Adjusting Batch Size

If throughput becomes a bottleneck:
- Increase batch size (100, 200)
- Decrease polling interval (15s, 10s)
- Or move to event-driven (notification created → immediate queue)

---

## Detailed Flow

```java
@Scheduled(fixedRate = 30000)
public void processDueNotifications() {
    List<Notification> dueNotifications = notificationRepository
        .findByStatusAndScheduledTimeBefore(
            NotificationStatus.PENDING,
            Instant.now(),
            PageRequest.of(0, 50)
        );

    for (Notification notification : dueNotifications) {
        processNotification(notification);
    }
}

private void processNotification(Notification notification) {
    List<PushSubscription> subscriptions = subscriptionRepository
        .findByUserId(notification.getRecipientId());

    if (subscriptions.isEmpty()) {
        notification.setStatus(NotificationStatus.FAILED);
        notificationRepository.save(notification);
        return;
    }

    boolean anySuccess = false;
    for (PushSubscription subscription : subscriptions) {
        SendResult result = pushService.send(
            subscription,
            NotificationPayload.from(notification)
        );

        switch (result.status()) {
            case SUCCESS -> anySuccess = true;
            case EXPIRED -> subscriptionRepository.delete(subscription);
            case FAILED -> { /* log, continue */ }
        }
    }

    if (anySuccess) {
        notification.setStatus(NotificationStatus.SENT);
    } else {
        scheduleRetry(notification);
    }

    notificationRepository.save(notification);
}
```

---

## Retry Logic

### Policy

| Attempt | Timing | Action |
|---|---|---|
| 1st | Immediate (within cycle) | Send to all subscriptions |
| 2nd (retry) | 60 seconds later | Send to remaining subscriptions |
| After 2nd failure | — | Mark as FAILED |

### Retry Implementation

```java
private void scheduleRetry(Notification notification) {
    if (!notification.isRetried()) {
        notification.setRetried(true);
        notification.setRetryAt(Instant.now().plusSeconds(60));
        notificationRepository.save(notification);
    } else {
        notification.setStatus(NotificationStatus.FAILED);
        notificationRepository.save(notification);
    }
}
```

### Retry Query

```sql
SELECT * FROM notifications
WHERE status = 'PENDING'
  AND scheduled_time <= NOW()
  AND (retry_at IS NULL OR retry_at <= NOW())
ORDER BY scheduled_time
LIMIT 50;
```

---

## Concurrency

### Problem

Multiple instances of the scheduler could pick up the same notification.

### Solution: Pessimistic Locking

```java
@Transactional
public List<Notification> lockAndGetDueNotifications() {
    return notificationRepository.queryForUpdate(
        NotificationStatus.PENDING,
        Instant.now(),
        PageRequest.of(0, 50)
    );
}
```

Or use `SELECT ... FOR UPDATE SKIP LOCKED` for non-blocking concurrent processing.

### For MVP

Single instance on Railway = no concurrency issue. But the code should be safe for future scaling.

---

## Monitoring

### Metrics to Track

| Metric | Description |
|---|---|
| `scheduler.cycle.duration` | Time per scheduling cycle (ms) |
| `scheduler.notifications.processed` | Number of notifications processed per cycle |
| `scheduler.push.success` | Successful push deliveries |
| `scheduler.push.failed` | Failed push deliveries |
| `scheduler.push.retried` | Notifications retried |
| `scheduler.subscriptions.cleaned` | Expired subscriptions removed |

### Alerting

| Condition | Action |
|---|---|
| Cycle duration > 10s | Investigate DB query performance |
| Success rate < 95% | Check push service health |
| Failed > 10/cycle | Check subscription cleanup |

---

## Future Improvements

| Change | When | Why |
|---|---|---|
| Increase batch to 200 | > 1K notifications/day | Higher throughput |
| Decrease interval to 15s | Need faster delivery | Lower latency |
| Move to event-driven | > 10K notifications/day | Real-time processing |
| Add distributed lock | Multiple instances | Prevent duplicate processing |
| Add dead letter queue | Need retry visibility | Track failed notifications |
