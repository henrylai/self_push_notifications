# PushPal — Product Principles

Seven principles that guide every product decision.

---

## 1. Speed Over Perfection

The core loop — from intent to scheduled notification — must take under 5 seconds. Every extra tap, every extra screen, every extra option is friction. Ship the smallest thing that works, then iterate.

> If a feature adds steps to the core loop, it must justify every single one.

## 2. Push Is the Product

Push notification reliability is everything. If a notification doesn't arrive, PushPal has failed. Every architectural decision must prioritize delivery reliability over feature breadth.

> A notification that doesn't arrive is worse than no notification at all.

## 3. Trust Through Transparency

Both sender and recipient must always know the status of a notification: sent, delivered, viewed, completed. No black holes. No ambiguity. Status is always visible and always accurate.

> If you can't see what happened to a notification, the system is broken.

## 4. Respecting the Recipient

The person receiving a notification is a first-class citizen. They can snooze, dismiss, decline, set quiet hours, and block anyone. PushPal is not a tool for harassment. The recipient always has control.

> The power to send a notification to someone else is a privilege, not a right.

## 5. Progressive Disclosure

Show the simplest experience first. Advanced features (recurring schedules, groups, quiet hours) are available but never forced. A new user should understand PushPal in 10 seconds.

> Complexity must be earned, not imposed.

## 6. Ship Like a Startup

PWA before native. Simple cron before Temporal. Monolith before microservices. Railway before Kubernetes. Speed of delivery beats architectural purity.

> Perfect architecture at launch is a failure if no one is using it.

## 7. Privacy Is Not Optional

Encrypt at rest and in transit. Let users delete their data anytime. Never log notification content. Never sell data. Privacy is a feature, not an afterthought.

> If you wouldn't want it on a billboard, don't store it unencrypted.
