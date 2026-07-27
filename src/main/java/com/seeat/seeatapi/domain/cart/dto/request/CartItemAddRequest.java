package com.seeat.seeatapi.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 3-2 장바구니 상품 추가
public record CartItemAddRequest(
        @NotNull Long productId,
        @Min(1) Integer quantity
) {}
