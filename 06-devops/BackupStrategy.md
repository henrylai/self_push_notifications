# PushPal — Backup Strategy

## Overview

Multi-layer backup strategy: Railway automatic backups + manual pg_dump + point-in-time restore.

---

## 1. Railway Automatic Backups

| Setting | Value |
|---|---|
| Frequency | Daily |
| Retention | 7 days |
| Storage | Railway-managed |
| Access | Railway dashboard > Database > Backups |

### What's Backed Up

- Full database snapshot
- All tables (users, relationships, subscriptions, notifications)
- Schema and data

### How to Restore

1. Go to Railway dashboard > pushpal-db > Backups
2. Select a backup
3. Click "Restore"
4. Database is restored to backup point in time

---

## 2. Weekly pg_dump to S3 (V2)

### Setup

```bash
#!/bin/bash
# scripts/backup-database.sh

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="pushpal_${TIMESTAMP}.sql.gz"

# Dump database
pg_dump $DATABASE_URL | gzip > /tmp/$BACKUP_FILE

# Upload to S3
aws s3 cp /tmp/$BACKUP_FILE s3://pushpal-backups/daily/$BACKUP_FILE

# Clean up local file
rm /tmp/$BACKUP_FILE

# Delete backups older than 30 days
aws s3 ls s3://pushpal-backups/daily/ | awk '{print $4}' | while read line; do
  if [[ $line < $(date -d "30 days ago" +%Y%m%d) ]]; then
    aws s3 rm s3://pushpal-backups/daily/$line
  fi
done
```

### Schedule

Run weekly via GitHub Actions:

```yaml
# .github/workflows/backup.yml
name: Database Backup

on:
  schedule:
    - cron: '0 2 * * 0'  # Every Sunday at 2am UTC

jobs:
  backup:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run backup script
        env:
          DATABASE_URL: ${{ secrets.DATABASE_URL }}
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        run: ./scripts/backup-database.sh
```

### S3 Configuration

| Setting | Value |
|---|---|
| Bucket | `pushpal-backups` |
| Region | `us-east-1` |
| Lifecycle | Delete after 30 days |
| Encryption | AES-256 |
| Access | IAM user with limited permissions |

---

## 3. Point-in-Time Restore (V2)

### Railway Support

Railway supports point-in-time restore for PostgreSQL:

1. Go to Railway dashboard > pushpal-db
2. Click "Point-in-Time Restore"
3. Select timestamp
4. Database is restored to that point in time

### Use Cases

- Accidental data deletion
- Corrupted data
- Migration rollback

---

## 4. What's NOT Backed Up

| Data | Why Not |
|---|---|
| Push subscription tokens | Can be re-registered |
| JWT tokens | Stateless, not stored |
| Frontend static files | On Vercel CDN, redeployable |
| Environment variables | Documented in secrets table |

---

## 5. Recovery Procedures

### Scenario: Database Corruption

1. Stop the API (Railway dashboard)
2. Restore from latest backup
3. Verify data integrity
4. Restart the API
5. Notify affected users (if any)

### Scenario: Accidental Data Deletion

1. Identify deletion timestamp from logs
2. Restore to point-in-time before deletion
3. Verify data integrity
4. Restart the API

### Scenario: Full Data Loss

1. Create new PostgreSQL instance on Railway
2. Restore from latest pg_dump backup
3. Update DATABASE_URL in Railway env vars
4. Restart the API
5. Run Flyway migrations if needed

---

## 6. Backup Verification

### Monthly Test

1. Restore backup to a staging database
2. Run application against restored data
3. Verify all tables exist and are populated
4. Test notification creation and delivery
5. Document results

### Automated Checks (V2)

```sql
-- Verify backup integrity
SELECT COUNT(*) as table_count
FROM information_schema.tables
WHERE table_schema = 'public';
-- Should return 4 (users, relationships, subscriptions, notifications)
```

---

## 7. Cost

| Item | Cost |
|---|---|
| Railway automatic backups | Included in plan |
| S3 storage (30 days) | ~$0.50/month |
| S3 requests | ~$0.10/month |
| **Total** | **~$0.60/month** |
