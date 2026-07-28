package com.seeat.seeatapi.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 1-6 관리자 OTP 등록 확인
public record OtpVerifyRequest(
        @NotBlank @Email String email,
        @NotBlank String otpCode
) {}