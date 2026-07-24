package com.seeat.seeatapi.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // 데이터가 있는 성공 응답 (예: 회원가입, 로그인 등)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "OK");
    }

    // 데이터 + 커스텀 메시지가 있는 성공 응답 (예: "회원가입이 완료되었습니다.")
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    // 데이터 없는 성공 응답 (예: 장바구니 삭제, 5-4 일부)
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}