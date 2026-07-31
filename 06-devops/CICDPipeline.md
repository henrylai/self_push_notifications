# PushPal — CI/CD Pipeline

## Overview

GitHub Actions for CI/CD. Three workflows: backend test, frontend build, deploy.

---

## Workflow 1: Backend Test

```yaml
# .github/workflows/backend-test.yml
name: Backend Test

on:
  push:
    paths:
      - 'backend/**'
  pull_request:
    paths:
      - 'backend/**'

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: pushpal_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run tests
        run: ./gradlew test
        env:
          DATABASE_URL: jdbc:postgresql://localhost:5432/pushpal_test
          DB_USERNAME: test
          DB_PASSWORD: test
          JWT_SECRET: test-secret-key-for-ci-only
          VAPID_PUBLIC_KEY: test-vapid-public
          VAPID_PRIVATE_KEY: test-vapid-private
          VAPID_SUBJECT: mailto:test@example.com

      - name: Build
        run: ./gradlew bootJar

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: backend-test-results
          path: build/reports/tests/
```

---

## Workflow 2: Frontend Build

```yaml
# .github/workflows/frontend-build.yml
name: Frontend Build

on:
  push:
    paths:
      - 'frontend/**'
  pull_request:
    paths:
      - 'frontend/**'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        run: npm ci
        working-directory: frontend

      - name: Lint
        run: npm run lint
        working-directory: frontend

      - name: Type check
        run: npm run typecheck
        working-directory: frontend

      - name: Build
        run: npm run build
        working-directory: frontend
        env:
          NEXT_PUBLIC_VAPID_PUBLIC_KEY: test-vapid-public
          NEXT_PUBLIC_API_URL: http://localhost:8080
```

---

## Workflow 3: Deploy

```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  push:
    branches:
      - main

jobs:
  deploy-backend:
    runs-on: ubuntu-latest
    if: contains(github.event.head_commit.modified, 'backend/')

    steps:
      - uses: actions/checkout@v4

      - name: Deploy to Railway
        uses: bervProject/railway-deploy@main
        with:
          railway_token: ${{ secrets.RAILWAY_TOKEN }}
          service: pushpal-api

  deploy-frontend:
    runs-on: ubuntu-latest
    if: contains(github.event.head_commit.modified, 'frontend/')

    steps:
      - uses: actions/checkout@v4

      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          working-directory: frontend
```

---

## Required Secrets

| Secret | Where to Set |
|---|---|
| `RAILWAY_TOKEN` | GitHub repo > Settings > Secrets |
| `VERCEL_TOKEN` | GitHub repo > Settings > Secrets |
| `VERCEL_ORG_ID` | GitHub repo > Settings > Secrets |
| `VERCEL_PROJECT_ID` | GitHub repo > Settings > Secrets |

---

## Pipeline Flow

```
Push to main
    |
    ├── backend/** changed?
    |   ├── Run backend tests (JUnit + Testcontainers)
    |   ├── Build JAR
    |   └── Deploy to Railway
    |
    └── frontend/** changed?
        ├── Run lint
        ├── Run typecheck
        ├── Build static export
        └── Deploy to Vercel
```

---

## Branch Protection Rules

| Rule | Setting |
|---|---|
| Require PR reviews | 1 approval |
| Require status checks | backend-test, frontend-build |
| Require branches to be up to date | Yes |
| Require conversation resolution | Yes |
| Include administrators | Yes |

---

## Quality Gates

Before merge, all of these must pass:

- [ ] Backend tests pass
- [ ] Backend builds successfully
- [ ] Frontend lint passes
- [ ] Frontend typecheck passes
- [ ] Frontend builds successfully
- [ ] PR has at least 1 approval
- [ ] All conversations resolved
