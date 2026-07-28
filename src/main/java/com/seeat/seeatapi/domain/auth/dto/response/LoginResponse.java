package com.seeat.seeatapi.domain.auth.dto.response;

// 1-2 로그인 응답
public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String role
) {
    public static LoginResponse of(String accessToken, String refreshToken, long expiresInSeconds, String role) {
        return new LoginResponse(accessToken, refreshToken, expiresInSeconds, role);
    }
}