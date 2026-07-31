package com.seeat.seeatapi.domain.auth.dto.request;

import com.seeat.seeatapi.global.common.validation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Pattern(regexp = "BUYER|SELLER", message = "role은 BUYER 또는 SELLER만 가능합니다.") String role,
        @NotBlank @Size(max = 50) String nickname,
        @NotBlank @PhoneNumber String phoneNumber
) {}