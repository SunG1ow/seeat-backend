package com.seeat.seeatapi.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

// 5-4 회원 탈퇴
public record WithdrawRequest(
        @NotBlank String password,
        String reason
) {}