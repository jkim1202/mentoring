ALTER TABLE applications
ADD COLUMN applied_mentee_user_id BIGINT
GENERATED ALWAYS AS (
  CASE
    WHEN status = 'APPLIED' THEN mentee_user_id
    ELSE NULL
  END
) VIRTUAL,
ADD COLUMN applied_slot_id BIGINT
GENERATED ALWAYS AS (
  CASE
    WHEN status = 'APPLIED' THEN slot_id
    ELSE NULL
  END
) VIRTUAL;

ALTER TABLE applications
ADD CONSTRAINT uk_applications_applied_mentee_slot
UNIQUE (applied_mentee_user_id, applied_slot_id);
