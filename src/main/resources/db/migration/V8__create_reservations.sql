CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL COMMENT '예약으로 이어진 신청',
    listing_id BIGINT NOT NULL COMMENT '예약 대상 등록글',
    slot_id BIGINT NOT NULL COMMENT '예약 확정 슬롯',
    mentor_user_id BIGINT NOT NULL COMMENT '멘토 사용자',
    mentee_user_id BIGINT NOT NULL COMMENT '멘티 사용자',
    start_at DATETIME NOT NULL COMMENT '예약 시작 시각 스냅샷',
    end_at DATETIME NOT NULL COMMENT '예약 종료 시각 스냅샷',
    status ENUM('PENDING_PAYMENT', 'CONFIRMED', 'CANCELED', 'COMPLETED') NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '예약 상태',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservations_application_id FOREIGN KEY (application_id) REFERENCES applications(id),
    CONSTRAINT fk_reservations_listing_id FOREIGN KEY (listing_id) REFERENCES listings(id),
    CONSTRAINT fk_reservations_slot_id FOREIGN KEY (slot_id) REFERENCES slots(id),
    CONSTRAINT fk_reservations_mentor_user_id FOREIGN KEY (mentor_user_id) REFERENCES users(id),
    CONSTRAINT fk_reservations_mentee_user_id FOREIGN KEY (mentee_user_id) REFERENCES users(id),
    CONSTRAINT chk_reservations_time CHECK (end_at > start_at),
    CONSTRAINT uk_reservations_application_id UNIQUE (application_id),
    CONSTRAINT uk_reservations_slot_id UNIQUE (slot_id)
) COMMENT = '멘토링 예약';

CREATE INDEX idx_reservations_mentor_status_start ON reservations(mentor_user_id, status, start_at);
CREATE INDEX idx_reservations_mentee_status_start ON reservations(mentee_user_id, status, start_at);
CREATE INDEX idx_reservations_listing_status ON reservations(listing_id, status);
