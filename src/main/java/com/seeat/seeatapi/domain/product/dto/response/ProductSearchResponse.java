package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.domain.product.entity.Product;

import java.math.BigDecimal;
import java.util.List;

// 2-3 필터 및 정렬 검색 응답 (목록 항목 하나)
public record ProductSearchResponse(
        Long productId,
        String name,
        BigDecimal price,
        String origin,
        BigDecimal weight,
        String weightUnit,
        List<String> tags,
        String thumbnailUrl
) {
    public static ProductSearchResponse of(Product product, List<String> tags, String thumbnailUrl) {
        return new ProductSearchResponse(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getOrigin(),
                product.getWeight(),
                product.getWeightUnit(),
                tags,
                thumbnailUrl
        );
    }
}