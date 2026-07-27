package com.seeat.seeatapi.domain.cart.dto.response;

// 3-2 장바구니 추가 응답
public record CartItemAddResponse(
        Long cartProductId,
        Long productId,
        int quantity
) {}