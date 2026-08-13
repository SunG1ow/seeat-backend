package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.domain.product.entity.Product;
import com.seeat.seeatapi.domain.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// [신규] 판매자 상품 목록 조회 응답 (GET /api/v1/seller/products)
public record ProductSellerListResponse(
        Long productId,
        String name,
        BigDecimal price,
        Integer stockQuantity,
        ProductStatus status,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
    public static ProductSellerListResponse of(Product product, String thumbnailUrl) {
        return new ProductSellerListResponse(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                thumbnailUrl,
                product.getCreatedAt()
        );
    }
}