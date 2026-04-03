ALTER TABLE reservations
ADD COLUMN mentee_paid_marked_at DATETIME NULL COMMENT '멘티 입금 완료 표시 시각',
ADD COLUMN mentor_paid_confirmed_at DATETIME NULL COMMENT '멘토 입금 확인 시각';
