CREATE TABLE reservation_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL COMMENT '메시지가 속한 예약',
    sender_user_id BIGINT NOT NULL COMMENT '메시지 발신자',
    content TEXT NOT NULL COMMENT '메시지 내용',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_messages_reservation_id
        FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_reservation_messages_sender_user_id
        FOREIGN KEY (sender_user_id) REFERENCES users(id)
) COMMENT = '예약 메시지';

CREATE INDEX idx_reservation_messages_reservation_created
    ON reservation_messages(reservation_id, created_at);

CREATE INDEX idx_reservation_messages_sender_created
    ON reservation_messages(sender_user_id, created_at);
