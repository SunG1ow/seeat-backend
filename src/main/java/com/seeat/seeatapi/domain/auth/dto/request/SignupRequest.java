package com.seeat.seeatapi.domain.auth.dto.request;

import com.seeat.seeatapi.global.common.validation.PhoneNumber;
import com.seeat.seeatapi.global.common.validation.ValidEnum;
import com.seeat.seeatapi.domain.member.entity.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 1-1 회원가입
public record SignupRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @ValidEnum(enumClass = MemberRole.class) String role,
        @NotBlank @Size(max = 50) String nickname,
        @NotBlank @PhoneNumber String phoneNumber
) {}