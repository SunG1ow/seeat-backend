package com.seeat.seeatapi.domain.member.dto.request;

import com.seeat.seeatapi.global.common.validation.PhoneNumber;

// 5-1 프로필 수정 (multipart/form-data, profileImage는 Controller에서 MultipartFile로 별도 처리)
public record UpdateProfileRequest(
        String nickname,
        @PhoneNumber String phoneNumber
) {}