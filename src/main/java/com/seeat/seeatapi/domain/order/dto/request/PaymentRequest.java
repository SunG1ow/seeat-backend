package com.seeat.seeatapi.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;

// 4-2 결제 시도/승인
public record PaymentRequest(
        @NotBlank String paymentMethod,
        @NotBlank String pgTransactionId
) {}