CREATE TABLE notification_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES push_subscriptions(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    failure_reason TEXT,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_notification_delivery_subscription
        UNIQUE (notification_id, subscription_id)
);

CREATE INDEX idx_notification_deliveries_notification
    ON notification_deliveries(notification_id);
CREATE INDEX idx_notification_deliveries_pending
    ON notification_deliveries(status, next_attempt_at)
    WHERE status = 'PENDING';
