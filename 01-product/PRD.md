# PushPal — Product Requirements Document (PRD)

## Problem Statement

There is no tool that lets you schedule a push notification to someone else's phone and know they saw it. Messaging apps require both parties to be available simultaneously. Reminder apps only work for yourself. There is no reliable way to say "remind my partner to do X at 5pm tomorrow" and have it actually happen.

## Proposed Solution

A Progressive Web App (PWA) with a Spring Boot backend that uses the Web Push API (VAPID) to deliver scheduled push notifications. Users can schedule self-reminders or send scheduled reminders to any linked Pal.

## Target Users (MVP)

- **Friends, family, and couples** who want reliable scheduled reminders
- **Parents** managing family schedules
- **Recipient model:** each reminder has one recipient; users may link with multiple Pals

## Success Metrics

| Metric | Target | Measurement |
|---|---|---|
| Daily active usage | >50% of registered users | Analytics |
| Time to schedule self-reminder | < 5 seconds | User testing |
| Time to send to a Pal | < 10 seconds | User testing |
| Push delivery rate | > 99% | Backend logs |
| User retention (weekly) | > 40% | Analytics |

## Functional Requirements

### Authentication
- FR-001: User can sign in with Google OAuth2
- FR-002: User can sign in with magic link (email)
- FR-003: Session persists via JWT (7-day expiry)

### Device Management
- FR-004: User can register a device for push notifications
- FR-005: User can register multiple devices (V2)
- FR-006: Stale subscriptions are cleaned up automatically

### Notifications
- FR-007: User can create a self-reminder
- FR-008: User can send a reminder to any linked Pal
- FR-009: User can view all sent and received notifications
- FR-010: User can cancel a pending notification
- FR-011: Notification status updates (sent → delivered → viewed)
- FR-012: Notifications are delivered at the scheduled time (±30s)

### Relationships
- FR-013: User can generate an invite code
- FR-014: User can accept an invite code to link with a Pal
- FR-015: Each user can link with multiple Pals (one recipient per reminder)

## Non-Functional Requirements

| Requirement | Target |
|---|---|
| API response time | < 200ms (p95) |
| Notification delivery latency | < 30 seconds from scheduled time |
| Push delivery rate | > 99% |
| Uptime | > 99.5% |
| Data encryption | TLS in transit, encrypted at rest |

## Out of Scope (V1)

See [00-vision/Non-Goals.md](../00-vision/Non-Goals.md) for full list.
