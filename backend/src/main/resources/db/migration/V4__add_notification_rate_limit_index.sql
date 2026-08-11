CREATE INDEX idx_notifications_sender_created_at
    ON notifications(sender_id, created_at);
