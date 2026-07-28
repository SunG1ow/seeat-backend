package com.seeat.seeatapi.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

// 5-2 배송지 추가
public record AddressRequest(
        @NotBlank String alias,
        @NotBlank String receiverName,
        String receiverPhone,
        @NotBlank String address,
        Boolean isDefault
) {}