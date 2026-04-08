package org.example.mentoring.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "APPLICATION_001", "해당 신청을 찾을 수 없습니다."),
    APPLICATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "APPLICATION_002", "해당 신청이 이미 존재합니다."),
    APPLICATION_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "APPLICATION_003", "신청 상태를 변경할 수 없습니다."),
    APPLICATION_NOT_BELONG_TO_MENTOR(HttpStatus.FORBIDDEN, "APPLICATION_004", "신청이 멘토의 것이 아닙니다."),
    APPLICATION_ACCEPT_EXPIRED(HttpStatus.BAD_REQUEST, "APPLICATION_005", "이미 시작 시간이 지난 신청은 수락할 수 없습니다."),

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
    LISTING_NOT_EDITABLE(HttpStatus.FORBIDDEN, "LISTING_002", "게시글을 수정할 수 없습니다."),
    LISTING_INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "LISTING_003", "최소 금액은 최대 금액보다 클 수 없습니다."),
    LISTING_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "LISTING_004", "게시글 상태를 변경할 수 없습니다."),

    SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "SLOT_001", "슬롯을 찾을 수 없습니다."),
    SLOT_NOT_BELONG_TO_LISTING(HttpStatus.BAD_REQUEST, "SLOT_002", "슬롯이 등록글에 속하지 않습니다."),
    SLOT_ALREADY_BOOKED(HttpStatus.CONFLICT, "SLOT_003", "이미 예약된 슬롯입니다."),
    SLOT_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "SLOT_004", "슬롯 상태를 변경할 수 없습니다."),

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_001", "예약을 찾을 수 없습니다."),
    RESERVATION_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "RESERVATION_002", "예약 상태를 변경할 수 없습니다."),
    RESERVATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "RESERVATION_003", "이미 존재하는 예약입니다."),
    RESERVATION_CANCEL_DEADLINE_EXCEEDED(HttpStatus.BAD_REQUEST, "RESERVATION_004", "멘티 취소 가능 시간이 지났습니다."),
    RESERVATION_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "RESERVATION_005", "아직 예약이 완료되지 않았습니다."),
    RESERVATION_MESSAGE_NOT_WRITABLE(HttpStatus.BAD_REQUEST, "RESERVATION_006", "현재 예약 상태에서는 메시지를 보낼 수 없습니다."),
    RESERVATION_PAYMENT_EXPIRED(HttpStatus.BAD_REQUEST, "RESERVATION_007", "입금 가능 시간이 지나 예약이 만료되었습니다."),
    RESERVATION_START_AT_EXPIRED(HttpStatus.BAD_REQUEST, "RESERVATION_008", "예약이 시작 시간을 넘어서 만료되었습니다."),

    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_001", "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_002", "이미 리뷰가 존재합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
