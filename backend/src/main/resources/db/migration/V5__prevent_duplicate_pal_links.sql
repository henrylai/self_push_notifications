CREATE UNIQUE INDEX idx_relationships_unique_accepted_pair
    ON user_relationships (
        LEAST(inviter_id, invitee_id),
        GREATEST(inviter_id, invitee_id)
    )
    WHERE status = 'ACCEPTED';
