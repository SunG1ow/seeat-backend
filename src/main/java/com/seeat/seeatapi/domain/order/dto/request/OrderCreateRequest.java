package com.seeat.seeatapi.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 4-1 주문서 작성
public record OrderCreateRequest(
        @NotEmpty @Valid List<OrderItemRequest> items,
        @NotNull Long addressId,
        String requestMessage
) {
    public record OrderItemRequest(
            @NotNull Long productId,
            @NotNull Integer quantity
    ) {}
}