package com.seeat.seeatapi.domain.order.dto.response;

// 4-2 결제 응답
public record PaymentResponse(
        Long paymentId,
        Long orderId,
        String orderStatus,
        String paymentMethod,
        String pgTransactionId
) {}