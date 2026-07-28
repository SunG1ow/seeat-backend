package com.seeat.seeatapi.domain.auth.dto.response;

import com.seeat.seeatapi.domain.member.entity.Member;

// 1-1 회원가입 응답
public record SignupResponse(
        Long userId,
        String email,
        String role
) {
    public static SignupResponse from(Member member) {
        return new SignupResponse(
                member.getUserId(),
                member.getEmail(),
                member.getRole().name()
        );
    }
}