package org.example.mentoring.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "로그인 정보가 일치하지 않습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "만료된 토큰입니다."),
    AUTH_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "지원하지 않는 토큰 타입/구조입니다."),
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_005", "권한이 부족합니다."),
    AUTH_STATUS_NOT_ACTIVE(HttpStatus.FORBIDDEN, "AUTH_006", "비활성화/삭제된 계정입니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_007", "접근할 수 없습니다."),

    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "유효하지 않은 입력입니다."),
    COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다."),

    LISTING_NOT_FOUND(HttpStatus.NOT_FOUND, "LISTING_001", "게시글을 찾을 수 없습니다."),
    LISTING_NOT_EDITABLE(HttpStatus.FORBIDDEN, "LISTING_002", "게시글을 수정할 수 없습니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
