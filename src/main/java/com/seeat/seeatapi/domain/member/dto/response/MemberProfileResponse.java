package com.seeat.seeatapi.domain.member.dto.response;

import com.seeat.seeatapi.domain.member.entity.Member;

// 5-1 프로필 조회/수정 응답
public record MemberProfileResponse(
        Long userId,
        String email,
        String nickname,
        String phoneNumber,
        String role
) {}