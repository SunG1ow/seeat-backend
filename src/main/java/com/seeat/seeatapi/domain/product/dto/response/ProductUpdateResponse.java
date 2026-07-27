package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.domain.product.entity.Product;

// 2-5 상품 수정 응답
public record ProductUpdateResponse(
        Long productId,
        String name
) {
    public static ProductUpdateResponse from(Product product) {
        return new ProductUpdateResponse(product.getProductId(), product.getName());
    }
}