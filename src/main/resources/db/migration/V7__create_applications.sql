CREATE TABLE applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL COMMENT '신청 대상 등록글',
    mentee_user_id BIGINT NOT NULL COMMENT '신청한 멘티',
    slot_id BIGINT NOT NULL COMMENT '신청 대상 슬롯',
    status ENUM('APPLIED', 'ACCEPTED', 'REJECTED', 'CANCELED') NOT NULL DEFAULT 'APPLIED' COMMENT '신청 상태',
    message TEXT NULL COMMENT '멘티 요청 메시지',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_applications_listing_id FOREIGN KEY (listing_id) REFERENCES listings(id),
    CONSTRAINT fk_applications_mentee_user_id FOREIGN KEY (mentee_user_id) REFERENCES users(id),
    CONSTRAINT fk_applications_slot_id FOREIGN KEY (slot_id) REFERENCES slots(id)
) COMMENT = '멘토링 신청';

CREATE INDEX idx_applications_listing_status ON applications(listing_id, status);
CREATE INDEX idx_applications_mentee_status ON applications(mentee_user_id, status);
CREATE INDEX idx_applications_slot_status ON applications(slot_id, status);
