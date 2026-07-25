package com.seeat.seeatapi.domain.auth.dto.response;

// 1-3 관리자 로그인 응답 (refreshToken 없음 - 명세서 확인 결과 관리자는 accessToken만 응답)
public record AdminLoginResponse(
        String accessToken,
        long expiresIn,
        String role
) {}