package com.seeat.seeatapi.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 1-3 관리자 로그인
public record AdminLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String otpCode
) {}