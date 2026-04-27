CREATE TABLE likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자',
    listing_id BIGINT NOT NULL COMMENT '찜한 게시글',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_likes_user_id
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_likes_listing_id
        FOREIGN KEY (listing_id) REFERENCES listings(id),
    CONSTRAINT uk_likes_user_id_listing_id UNIQUE (user_id, listing_id)
) COMMENT = '찜';

CREATE INDEX idx_likes_user_created
    ON likes(user_id, created_at);
