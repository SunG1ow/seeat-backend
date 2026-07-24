package com.seeat.seeatapi.global.exception;

import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;

@Getter
@Builder
public class ErrorResponse {
    private boolean success;
    private String timestamp;
    private int status;
    private String code;
    private String message;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .success(false)
                .timestamp(OffsetDateTime.now().toString())
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return ErrorResponse.builder()
                .success(false)
                .timestamp(OffsetDateTime.now().toString())
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(customMessage)
                .build();
    }
}