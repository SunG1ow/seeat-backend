package com.seeat.seeatapi.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;

// 4-3 주문 상태 실시간 변경
public record OrderStatusChangeRequest(
        @NotBlank String status,
        String carrier,
        String trackingNumber
) {}