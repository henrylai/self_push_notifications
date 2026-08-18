ALTER TABLE push_subscriptions
    ADD COLUMN revoked BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN revocation_reason VARCHAR(20);

CREATE INDEX idx_subscriptions_user_active
    ON push_subscriptions(user_id)
    WHERE revoked = FALSE;
