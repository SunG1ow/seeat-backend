package com.seeat.seeatapi.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 1-2 일반 로그인
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}