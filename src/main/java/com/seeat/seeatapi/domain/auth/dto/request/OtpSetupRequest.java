package com.seeat.seeatapi.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 1-5 관리자 OTP 최초 등록
public record OtpSetupRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}