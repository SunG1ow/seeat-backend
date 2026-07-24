package com.seeat.seeatapi.global.exception;

import java.time.OffsetDateTime;

public class ErrorResponse {

    private final boolean success = false;
    private final OffsetDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;

    private ErrorResponse(int status, String code, String message) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), message);
    }

    public boolean isSuccess() {
        return success;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}