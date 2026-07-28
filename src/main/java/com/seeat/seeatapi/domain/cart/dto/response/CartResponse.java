package com.seeat.seeatapi.domain.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;

// 3-1 장바구니 조회 응답
public record CartResponse(
        Long cartId,
        List<CartItemResponse> items
) {
    public record CartItemResponse(
            Long cartProductId,
            Long productId,
            String productName,
            int quantity,
            BigDecimal price
    ) {}
}