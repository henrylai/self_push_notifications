# PushPal — Scheduler Design

## Overview

The Scheduler Service polls the database every 10 seconds by default for notifications that are due
and triggers push delivery. `SCHEDULER_INTERVAL_MS` can tune the interval.

---

## Polling Flow

```
Every 10 seconds (`SCHEDULER_INTERVAL_MS`, default 10000)
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
│     - Expired sub → REVOKE sub   │
│     - Device failure → Retry once│
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
- At 10s intervals, 50 notifications = ~300/minute throughput
- Sufficient for MVP (expected < 1K notifications/day)
- Each notification may hit 1-2 subscriptions
- Total push sends per cycle: ~50-100

### Adjusting Batch Size

If throughput becomes a bottleneck:
- Increase batch size (100, 200)
- Decrease the configured polling interval
- Or move to event-driven (notification created → immediate queue)

---

## Detailed Flow

```java
@Scheduled(fixedDelayString = "${app.scheduler.interval-ms:10000}")
public void processPendingNotifications() {
    Page<Notification> pending = notificationService.getPendingNotifications(
        Instant.now(), PageRequest.of(0, 50));
    pending.forEach(notification -> deliveryService.process(notification.getId()));
}

@Transactional
public void process(UUID notificationId) {
    Notification notification = notificationService
        .lockDueNotification(notificationId, Instant.now())
        .orElse(null);
    List<PushSubscription> activeSubscriptions = subscriptionRepository
        .findByUserIdAndRevokedFalse(notification.getRecipientId());
    List<NotificationDelivery> deliveries = ensureDeliveryRows(
        notification, activeSubscriptions);
    List<PushSubscription> targets = pendingDueSubscriptions(deliveries);

    applyResults(pushService.sendToAll(targets, payload(notification)), deliveries);
    finalizeOrRetry(notification, deliveries);
}
```

---

## Retry Logic

### Policy

| Attempt | Timing | Action |
|---|---|---|
| 1st | Immediate (within cycle) | Send to all subscriptions |
| 2nd (retry) | 60 seconds later | Send only to subscriptions that failed |
| After 2nd failure | — | Mark as FAILED |

### Retry Implementation

Each delivery row records its attempt count, next attempt, final status, and failure reason. A
notification stays `PENDING` while any transient device failure is eligible for its one retry. It
becomes `SENT` after every actionable device resolves successfully or becomes invalid, and becomes
`FAILED` when a valid device still fails after the retry (or no device can receive it).

### Retry Query

```sql
SELECT * FROM notifications
WHERE status = 'PENDING'
  AND ((retry_count = 0 AND scheduled_time <= NOW())
    OR (retry_count > 0 AND next_attempt_at <= NOW()))
ORDER BY COALESCE(next_attempt_at, scheduled_time)
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
