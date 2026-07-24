package com.seeat.seeatapi.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION", "잘못된 상태 전이 요청입니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 일치하지 않습니다."),
    INVALID_OTP(HttpStatus.UNAUTHORIZED, "INVALID_OTP", "OTP 코드가 일치하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다."),
    IP_NOT_ALLOWED(HttpStatus.FORBIDDEN, "IP_NOT_ALLOWED", "허용되지 않은 IP 접속입니다."),

    // 404 Not Found
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "존재하지 않는 장바구니 항목입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "존재하지 않는 주문입니다."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "존재하지 않는 배송지입니다."),
    TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "TARGET_NOT_FOUND", "신고 대상을 찾을 수 없습니다."),

    // 409 Conflict
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "OUT_OF_STOCK", "재고가 부족합니다."),
    ADDRESS_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "ADDRESS_LIMIT_EXCEEDED", "배송지는 최대 5개까지 등록 가능합니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "ALREADY_REPORTED", "이미 신고한 대상입니다."),

    // 500 / 502 Internal & Gateway Errors
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    NTS_API_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "NTS_API_UNAVAILABLE", "국세청 API 연동 실패");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}