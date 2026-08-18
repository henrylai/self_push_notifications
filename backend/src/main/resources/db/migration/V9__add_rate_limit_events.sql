CREATE TABLE rate_limit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rate_limit_events_lookup
    ON rate_limit_events(user_id, action, created_at);

CREATE INDEX idx_rate_limit_events_created_at
    ON rate_limit_events(created_at);
