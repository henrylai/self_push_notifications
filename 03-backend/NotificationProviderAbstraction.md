# PushPal — Notification Provider Abstraction

## Purpose

Abstract the notification delivery mechanism so we can swap providers without changing business logic. Start with Web Push (VAPID), but make it easy to add FCM, APNs, or email later.

---

## Interface

```java
public interface NotificationProvider {

    /**
     * Send a notification to a device.
     *
     * @param payload The notification content
     * @param subscription Device subscription info
     * @return Result of the send attempt
     */
    SendResult send(NotificationPayload payload, SubscriptionInfo subscription);

    /**
     * Check if this provider can handle the given subscription.
     *
     * @param subscription The subscription to check
     * @return true if this provider handles the subscription type
     */
    boolean supports(SubscriptionInfo subscription);
}
```

---

## Data Types

### NotificationPayload

```java
public record NotificationPayload(
    String title,
    String body,
    String icon,        // optional
    String badge,       // optional
    String tag,         // for deduplication
    Map<String, String> data // arbitrary data
) {
    public static NotificationPayload from(Notification notification) {
        return new NotificationPayload(
            notification.getTitle(),
            notification.getBody(),
            "/icons/notification.png",
            "/icons/badge.png",
            "notification-" + notification.getId(),
            Map.of("notificationId", notification.getId().toString())
        );
    }
}
```

### SubscriptionInfo

```java
public record SubscriptionInfo(
    String endpoint,
    String p256dh,
    String auth
) {
    public static SubscriptionInfo from(PushSubscription subscription) {
        return new SubscriptionInfo(
            subscription.getEndpoint(),
            subscription.getP256dh(),
            subscription.getAuth()
        );
    }
}
```

### SendResult

```java
public record SendResult(
    Status status,
    String errorMessage
) {
    public enum Status {
        SUCCESS,
        FAILED,
        EXPIRED
    }

    public static SendResult success() {
        return new SendResult(Status.SUCCESS, null);
    }

    public static SendResult failed(String message) {
        return new SendResult(Status.FAILED, message);
    }

    public static SendResult expired() {
        return new SendResult(Status.EXPIRED, "Subscription expired (410)");
    }
}
```

---

## Implementations

### WebPushProvider (MVP)

```java
@Component
public class WebPushProvider implements NotificationProvider {

    private final WebPushClient webPushClient;

    public WebPushProvider(WebPushConfig config) {
        this.webPushClient = new WebPushClient(
            config.getVapidPublicKey(),
            config.getVapidPrivateKey(),
            config.getVapidSubject()
        );
    }

    @Override
    public SendResult send(NotificationPayload payload, SubscriptionInfo subscription) {
        try {
            WebPushMessage message = WebPushMessage.builder()
                .title(payload.title())
                .body(payload.body())
                .icon(payload.icon())
                .badge(payload.badge())
                .tag(payload.tag())
                .data(payload.data())
                .build();

            webPushClient.send(subscription.endpoint(), subscription.p256dh(), subscription.auth(), message);
            return SendResult.success();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 410) {
                return SendResult.expired();
            }
            return SendResult.failed(e.getMessage());

        } catch (Exception e) {
            return SendResult.failed(e.getMessage());
        }
    }

    @Override
    public boolean supports(SubscriptionInfo subscription) {
        return subscription.endpoint().contains("fcm.googleapis.com")
            || subscription.endpoint().contains("web.push.org")
            || subscription.endpoint().contains("android.googleapis.com");
    }
}
```

### Future: FCMProvider

```java
@Component
@ConditionalOnProperty(name = "notification.provider.fcm", havingValue = "true")
public class FCMProvider implements NotificationProvider {
    // Firebase Cloud Messaging implementation
    // Handles Android native and FCM web push
}
```

### Future: APNsProvider

```java
@Component
@ConditionalOnProperty(name = "notification.provider.apns", havingValue = "true")
public class APNsProvider implements NotificationProvider {
    // Apple Push Notification Service implementation
    // Handles iOS native notifications
}
```

### Future: EmailProvider

```java
@Component
@ConditionalOnProperty(name = "notification.provider.email", havingValue = "true")
public class EmailProvider implements NotificationProvider {
    // Email fallback implementation
    // Sends email when push fails
}
```

---

## Provider Selection

```java
@Service
public class NotificationDeliveryService {

    private final List<NotificationProvider> providers;

    public NotificationDeliveryService(List<NotificationProvider> providers) {
        this.providers = providers;
    }

    public SendResult deliver(NotificationPayload payload, SubscriptionInfo subscription) {
        return providers.stream()
            .filter(provider -> provider.supports(subscription))
            .findFirst()
            .map(provider -> provider.send(payload, subscription))
            .orElse(SendResult.failed("No provider supports this subscription"));
    }
}
```

---

## Configuration

```yaml
# application.yml
notification:
  provider:
    webpush:
      vapid-public-key: ${VAPID_PUBLIC_KEY}
      vapid-private-key: ${VAPID_PRIVATE_KEY}
      vapid-subject: mailto:pushpal@example.com
    fcm:
      enabled: false
      # project-id: ${FCM_PROJECT_ID}
    email:
      enabled: false
      # smtp-host: ${SMTP_HOST}
```

---

## Testing

### Mock Provider

```java
@Component
@Profile("test")
public class MockNotificationProvider implements NotificationProvider {

    private final List<SendResult> results = new ArrayList<>();

    @Override
    public SendResult send(NotificationPayload payload, SubscriptionInfo subscription) {
        SendResult result = results.isEmpty() ? SendResult.success() : results.remove(0);
        return result;
    }

    @Override
    public boolean supports(SubscriptionInfo subscription) {
        return true;
    }

    public void enqueueResult(SendResult result) {
        results.add(result);
    }
}
```
