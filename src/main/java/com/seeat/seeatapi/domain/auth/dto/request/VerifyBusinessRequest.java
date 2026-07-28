package com.seeat.seeatapi.domain.auth.dto.request;

import com.seeat.seeatapi.global.common.validation.BusinessRegistrationNumber;
import jakarta.validation.constraints.NotBlank;

// 1-4 판매자 사업자 인증
public record VerifyBusinessRequest(
        @NotBlank @BusinessRegistrationNumber String businessRegistrationNumber,
        @NotBlank String representativeName,
        @NotBlank String openingDate // yyyyMMdd 형식, Service에서 LocalDate로 파싱
) {}