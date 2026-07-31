# PushPal — Backend Project Structure

## Directory Tree

```
backend/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── pushpal/
│   │   │           ├── PushPalApplication.java
│   │   │           ├── config/
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   ├── CorsConfig.java
│   │   │           │   ├── WebPushConfig.java
│   │   │           │   ├── RateLimitConfig.java
│   │   │           │   └── JacksonConfig.java
│   │   │           ├── auth/
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── JwtService.java
│   │   │           │   ├── GoogleOAuthService.java
│   │   │           │   ├── MagicLinkService.java
│   │   │           │   ├── AuthFilter.java
│   │   │           │   └── dto/
│   │   │           │       ├── GoogleAuthRequest.java
│   │   │           │       ├── MagicLinkRequest.java
│   │   │           │       ├── AuthResponse.java
│   │   │           │       └── VerifyRequest.java
│   │   │           ├── user/
│   │   │           │   ├── UserController.java
│   │   │           │   ├── UserService.java
│   │   │           │   ├── User.java
│   │   │           │   ├── UserRepository.java
│   │   │           │   └── dto/
│   │   │           │       ├── UserResponse.java
│   │   │           │       └── UpdateUserRequest.java
│   │   │           ├── relationship/
│   │   │           │   ├── RelationshipController.java
│   │   │           │   ├── RelationshipService.java
│   │   │           │   ├── UserRelationship.java
│   │   │           │   ├── RelationshipRepository.java
│   │   │           │   └── dto/
│   │   │           │       ├── InviteResponse.java
│   │   │           │       ├── AcceptRequest.java
│   │   │           │       └── RelationshipResponse.java
│   │   │           ├── notification/
│   │   │           │   ├── NotificationController.java
│   │   │           │   ├── NotificationService.java
│   │   │           │   ├── Notification.java
│   │   │           │   ├── NotificationStatus.java
│   │   │           │   ├── NotificationRepository.java
│   │   │           │   └── dto/
│   │   │           │       ├── CreateNotificationRequest.java
│   │   │           │       ├── NotificationResponse.java
│   │   │           │       └── NotificationListResponse.java
│   │   │           ├── device/
│   │   │           │   ├── DeviceController.java
│   │   │           │   ├── DeviceService.java
│   │   │           │   ├── PushSubscription.java
│   │   │           │   ├── PushSubscriptionRepository.java
│   │   │           │   └── dto/
│   │   │           │       ├── RegisterDeviceRequest.java
│   │   │           │       └── DeviceResponse.java
│   │   │           ├── push/
│   │   │           │   ├── PushService.java
│   │   │           │   ├── NotificationProvider.java
│   │   │           │   ├── WebPushProvider.java
│   │   │           │   ├── NotificationPayload.java
│   │   │           │   ├── SubscriptionInfo.java
│   │   │           │   └── SendResult.java
│   │   │           ├── scheduler/
│   │   │           │   └── SchedulerService.java
│   │   │           └── common/
│   │   │               ├── GlobalExceptionHandler.java
│   │   │               ├── ErrorResponse.java
│   │   │               ├── RateLimiter.java
│   │   │               └── CurrentUser.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__create_users.sql
│   │               ├── V2__create_user_relationships.sql
│   │               ├── V3__create_push_subscriptions.sql
│   │               └── V4__create_notifications.sql
│   └── test/
│       └── java/
│           └── com/
│               └── pushpal/
│                   ├── auth/
│                   │   ├── AuthServiceTest.java
│                   │   ├── JwtServiceTest.java
│                   │   └── MagicLinkServiceTest.java
│                   ├── user/
│                   │   └── UserServiceTest.java
│                   ├── relationship/
│                   │   └── RelationshipServiceTest.java
│                   ├── notification/
│                   │   └── NotificationServiceTest.java
│                   ├── device/
│                   │   └── DeviceServiceTest.java
│                   ├── push/
│                   │   ├── PushServiceTest.java
│                   │   └── MockNotificationProvider.java
│                   ├── scheduler/
│                   │   └── SchedulerServiceTest.java
│                   └── integration/
│                       ├── AuthIntegrationTest.java
│                       ├── NotificationIntegrationTest.java
│                       └── PushIntegrationTest.java
└── Dockerfile
```

---

## Key Files

### build.gradle.kts

```kotlin
plugins {
    java
    org.springframework.boot
    io.spring.dependency-management
}

group = "com.pushpal"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
    implementation("nl.martijndwars:web-push:5.4.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
}
```

### application.yml

```yaml
spring:
  application:
    name: pushpal-api
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET}
  expiry: 7d

google:
  client-id: ${GOOGLE_CLIENT_ID}
  client-secret: ${GOOGLE_CLIENT_SECRET}

magic-link:
  secret: ${MAGIC_LINK_SECRET}
  expiry: 15m

webpush:
  vapid-public-key: ${VAPID_PUBLIC_KEY}
  vapid-private-key: ${VAPID_PRIVATE_KEY}
  vapid-subject: mailto:pushpal@example.com

scheduler:
  interval: 30000
  batch-size: 50

logging:
  level:
    com.pushpal: INFO
  pattern:
    console: "%d{yyyy-MM-dd'T'HH:mm:ss'Z'} %-5level %logger{36} - %msg%n"
```
