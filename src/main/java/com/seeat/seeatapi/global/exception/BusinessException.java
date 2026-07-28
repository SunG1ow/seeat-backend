package com.seeat.seeatapi.global.exception;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage;

    // 기본 메시지(ErrorCode에 정의된 메시지) 그대로 사용
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = errorCode.getMessage();
    }

    // 상황에 맞는 커스텀 메시지로 덮어쓰고 싶을 때 (예: "완도산 활전복 재고가 부족합니다.")
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}