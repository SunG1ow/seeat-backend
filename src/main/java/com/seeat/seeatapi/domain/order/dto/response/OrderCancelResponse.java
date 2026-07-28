package com.seeat.seeatapi.domain.order.dto.response;

// 4-7 구매자 취소 응답
public record OrderCancelResponse(
        Long orderId,
        String orderStatus
) {}