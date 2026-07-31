# PushPal

Scheduled push notifications between people.

## What is this?

PushPal lets you schedule a push notification to arrive on your own phone or someone else's phone at a specific time. Think "WhatsApp + Reminders" — fast to send like a text, reliable like an alarm.

## Tech Stack

- **Backend:** Spring Boot 3 + Java 21
- **Frontend:** Next.js 14 (PWA) + TypeScript
- **Database:** PostgreSQL
- **Notifications:** Web Push (VAPID)
- **Hosting:** Railway

## Project Structure

```
self_push_notifications/
├── 00-vision/          # Why this exists
├── 01-product/         # What to build and for whom
├── 02-architecture/    # How it's structured
├── 03-backend/         # API spec, DB schema, domain model
├── 04-frontend/        # PWA architecture, UI guidelines
├── 05-security/        # Threat model, auth design
├── 06-devops/          # Deployment, CI/CD
├── 07-quality/         # Testing, definition of done
├── 08-ai/              # AI agent instructions, coding standards
├── 09-project/         # Epics, milestones, issue breakdown
├── backend/            # Spring Boot application
├── frontend/           # Next.js application
├── README.md           # This file
└── AGENTS.md           # AI coding agent instructions
```

## Quick Start

### Prerequisites

- Java 21
- Node.js 20+
- PostgreSQL (local or Railway)
- Gradle 8.10+ (or use the wrapper)

### Backend

```bash
cd backend

# Generate Gradle wrapper (if not present)
gradle wrapper

# Run the application
./gradlew bootRun
# On Windows:
gradlew.bat bootRun
```

Backend starts on `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on `http://localhost:3000`.

### Environment Variables

Copy `.env.example` files and fill in:

**Backend** (set in Railway or as env vars):
- `DATABASE_URL` — PostgreSQL connection string
- `JWT_SECRET` — Secret for JWT signing (min 256 bits)
- `VAPID_PUBLIC_KEY` — Web Push VAPID public key
- `VAPID_PRIVATE_KEY` — Web Push VAPID private key
- `VAPID_SUBJECT` — Contact email for VAPID
- `GOOGLE_CLIENT_ID` — Google OAuth client ID
- `MAGIC_LINK_SECRET` — Secret for magic link tokens
- `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASS` — Email for magic links

**Frontend**:
- `NEXT_PUBLIC_API_URL` — Backend URL (default: `http://localhost:8080`)
- `NEXT_PUBLIC_VAPID_PUBLIC_KEY` — Same VAPID public key as backend

### Generate VAPID Keys

```bash
npx web-push generate-vapid-keys
```

## Documentation

Start with [00-vision/Vision.md](00-vision/Vision.md) for the product overview, or [08-ai/AgentInstructions.md](08-ai/AgentInstructions.md) if you're an AI agent working on this codebase.

## License

Private — not for distribution.
