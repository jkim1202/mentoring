CREATE TABLE availability_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL COMMENT '어떤 등록글의 시간인지',
    start_at DATETIME NOT NULL COMMENT '시작',
    end_at DATETIME NOT NULL COMMENT '종료',
    status ENUM('OPEN', 'BLOCKED', 'BOOKED') NOT NULL COMMENT '슬롯 상태',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 시간 무결성 체크
    CONSTRAINT chk_slot_time CHECK (end_at > start_at),
    CONSTRAINT fk_slots_listing_id FOREIGN KEY (listing_id) REFERENCES listings(id)
) COMMENT = '멘토링 가능 시간 슬롯';

-- 특정 글의 상태별 시간 탐색용
CREATE INDEX IDX_slots_listing_status_start ON availability_slots(listing_id, status, start_at);
-- 전체 서비스의 시간 탐색용
CREATE INDEX IDX_slots_status_start ON availability_slots(status, start_at);