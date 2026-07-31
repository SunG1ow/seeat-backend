package com.seeat.seeatapi.domain.member.dto.response;

import com.seeat.seeatapi.domain.member.entity.Member;

import java.time.LocalDateTime;

public record WithdrawResponse(
        Long userId,
        boolean isWithdrawn,
        LocalDateTime withdrawnAt
) {
    public static WithdrawResponse from(Member member) {
        return new WithdrawResponse(member.getUserId(), member.isWithdrawn(), member.getWithdrawnAt());
    }
}