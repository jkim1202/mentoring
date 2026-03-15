CREATE TABLE listings (
    -- 기본 키
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 외래 키 및 기본 정보
    mentor_user_id BIGINT NOT NULL COMMENT '작성자',
    title VARCHAR(120) NOT NULL COMMENT '제목',
    topic VARCHAR(80) NOT NULL COMMENT '예: Java/Spring/면접',
    price INT NOT NULL COMMENT '원 단위(0 허용 가능)',

    -- 장소 및 상세 내용
    place_type ENUM('ONLINE', 'OFFLINE', 'BOTH') NOT NULL COMMENT '장소 타입',
    place_desc VARCHAR(255) NULL COMMENT '오프라인 상세/온라인 툴',
    description TEXT NOT NULL COMMENT '상세 소개',

    -- 상태 및 캐시 정보
    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL COMMENT '노출 여부',
    avg_rating DECIMAL(3, 2) NOT NULL DEFAULT 0.00 COMMENT '정렬용 캐시',
    review_count INT NOT NULL DEFAULT 0 COMMENT '정렬용 캐시',

    -- 시간 정보
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 외래 키 제약 조건
    CONSTRAINT fk_listings_mentor FOREIGN KEY (mentor_user_id) REFERENCES users(id)
);

CREATE INDEX IDX_listings_topic ON listings(topic);
CREATE INDEX IDX_listings_status_created ON listings(status, created_at);

-- 검색 최적화 인덱스
CREATE INDEX IDX_listings_status_rating ON listings(status, avg_rating, review_count);
CREATE INDEX IDX_listings_status_review_count ON listings(status, review_count, avg_rating);