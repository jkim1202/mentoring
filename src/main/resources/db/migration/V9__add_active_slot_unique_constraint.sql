ALTER TABLE reservations
DROP FOREIGN KEY fk_reservations_slot_id;

ALTER TABLE reservations
DROP INDEX uk_reservations_slot_id;

ALTER TABLE reservations
ADD INDEX idx_reservations_slot_id (slot_id);

ALTER TABLE reservations
ADD CONSTRAINT fk_reservations_slot_id
FOREIGN KEY (slot_id) REFERENCES slots(id);

ALTER TABLE reservations
ADD COLUMN active_slot_id BIGINT
GENERATED ALWAYS AS (
  CASE
    WHEN status IN ('PENDING_PAYMENT', 'CONFIRMED') THEN slot_id
    ELSE NULL
  END
 ) VIRTUAL;

ALTER TABLE reservations
ADD CONSTRAINT uk_reservations_active_slot
UNIQUE (active_slot_id);
