ALTER TABLE notifications
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0,
    ADD COLUMN next_attempt_at TIMESTAMPTZ;

CREATE INDEX idx_notifications_pending_due ON notifications(status, next_attempt_at)
    WHERE status = 'PENDING';
