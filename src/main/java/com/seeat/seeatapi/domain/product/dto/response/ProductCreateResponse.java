package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.domain.product.entity.Product;

import java.util.List;

// 2-1 상품 등록 응답
public record ProductCreateResponse(
        Long productId,
        String status,
        List<String> imageUrls
) {
    public static ProductCreateResponse of(Product product, List<String> imageUrls) {
        return new ProductCreateResponse(
                product.getProductId(),
                product.getStatus().name(),
                imageUrls
        );
    }
}