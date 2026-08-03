package com.seeat.seeatapi.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "허용되지 않은 상태 전이입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 인증 / 회원 (1장)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.LOCKED, "로그인 실패 누적으로 계정이 잠겼습니다."),
    INVALID_OTP(HttpStatus.UNAUTHORIZED, "OTP 코드가 일치하지 않습니다."),
    IP_NOT_ALLOWED(HttpStatus.FORBIDDEN, "허용되지 않은 접속 IP입니다."),
    VERIFICATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "국세청 API 검증 결과가 일치하지 않습니다."),
    NTS_API_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "국세청 API 연동에 실패했습니다."),
    OTP_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 OTP가 등록된 계정입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    BUSINESS_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 사업자등록번호입니다."),

    // 상품 (2장)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    RESTRICTED_SPECIES(HttpStatus.FORBIDDEN, "의무 위판 어종은 직거래 등록이 제한됩니다."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "이미지 용량이 초과되었습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 이미지 형식입니다."),
    IMAGE_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_ENTITY, "이미지는 최대 5장까지 등록할 수 있습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 이미지입니다."),

    // 장바구니 (3장)
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장바구니 항목입니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),

    // 주문 / 결제 / 배송 (4장)
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    DUPLICATE_PG_TRANSACTION(HttpStatus.CONFLICT, "이미 처리된 PG 거래입니다."),
    PG_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "외부 PG사 연동에 실패했습니다."),
    NOTIFICATION_SEND_FAILED(HttpStatus.BAD_GATEWAY, "알림 발송에 실패했습니다."),

    // 사용자 (5장)
    ADDRESS_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "배송지는 최대 5개까지 등록할 수 있습니다."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 배송지입니다."),
    PENDING_ORDER_EXISTS(HttpStatus.CONFLICT, "미완료 주문이 존재하여 처리할 수 없습니다."),
    PENDING_SETTLEMENT_EXISTS(HttpStatus.CONFLICT, "미완료 정산이 존재하여 처리할 수 없습니다."),

    // 신고 (6장)
    TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 대상이 존재하지 않습니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "이미 동일 대상을 신고했습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신고입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}