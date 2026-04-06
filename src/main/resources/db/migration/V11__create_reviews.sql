CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL COMMENT '후기가 연결된 예약',
    listing_id BIGINT NOT NULL COMMENT '집계 대상 등록글',
    reviewer_user_id BIGINT NOT NULL COMMENT '후기 작성 사용자',
    rating TINYINT NOT NULL COMMENT '평점(1~5)',
    content TEXT NULL COMMENT '후기 내용',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_reservation_id FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_reviews_listing_id FOREIGN KEY (listing_id) REFERENCES listings(id),
    CONSTRAINT fk_reviews_reviewer_user_id FOREIGN KEY (reviewer_user_id) REFERENCES users(id),
    CONSTRAINT uk_reviews_reservation_id UNIQUE (reservation_id),
    CONSTRAINT chk_reviews_rating_range CHECK (rating BETWEEN 1 AND 5)
) COMMENT = '멘토링 후기';

CREATE INDEX idx_reviews_listing_created ON reviews(listing_id, created_at);
CREATE INDEX idx_reviews_reviewer_created ON reviews(reviewer_user_id, created_at);
