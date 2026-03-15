CREATE TABLE mentor_profiles (

    -- 키 및 정보
    user_id BIGINT NOT NULL COMMENT '사용자 ID (users 테이블 FK)',
    bio TEXT NULL COMMENT '멘토 자기소개 (긴 글)',
    career_years INT NULL COMMENT '경력 연수 (필터링용)',
    major VARCHAR(100) NULL COMMENT '전공 분야 (필터링용)',
    current_company VARCHAR(120) NULL COMMENT '현재 재직 중인 회사 (필터링용)',

    -- 자격증, 수상 이력 등 구조가 가변적인 데이터는 JSON으로 관리
    specs_json JSON NULL COMMENT '자유 확장 정보(자격증/포트폴리오/수상내역 등)',

    base_location VARCHAR(255) NULL COMMENT '활동 기준 지역 (예: 강남, 판교 등)',
    verified_flag BOOLEAN NOT NULL DEFAULT 0 COMMENT '관리자 인증 여부 (0: 미인증, 1: 인증)',

    -- 생성 및 수정 시간
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일',

    -- 제약 조건
    CONSTRAINT pk_mentor_profiles PRIMARY KEY (user_id), -- 1:1 관계이므로 user_id가 곧 PK
    CONSTRAINT fk_mentor_profiles_user_id FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT = '멘토 상세 프로필 정보';

-- 검색 최적화 인덱스
CREATE INDEX idx_mentor_profiles_career_years ON mentor_profiles(career_years);
CREATE INDEX idx_mentor_profiles_major ON mentor_profiles(major);
CREATE INDEX idx_mentor_profiles_current_company ON mentor_profiles(current_company);