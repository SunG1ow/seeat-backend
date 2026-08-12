package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.domain.product.entity.Product;
import com.seeat.seeatapi.domain.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 2-3 상품 상세 조회 응답
public record ProductDetailResponse(
        Long productId,
        Long sellerId,
        String sellerName,
        Long categoryId,
        String categoryName,
        String name,
        String origin,
        String storageType,
        BigDecimal weight,
        String weightUnit,
        Boolean isMandatoryAuction,
        BigDecimal price,
        Integer stockQuantity,
        LocalDateTime auctionDeadline, // [신규] 위판 마감 시각
        String description,            // [신규] 상품 상세 설명
        ProductStatus status,
        List<ProductImageResponse> images,
        List<String> tags,
        LocalDateTime createdAt
) {
    // ProductDetailResponse.java 의 from() 메서드 내부

    public static ProductDetailResponse from(Product product, List<ProductImageResponse> images, List<String> tags) {
        return new ProductDetailResponse(
                product.getProductId(),
                product.getSeller().getUserId(),
                product.getSeller().getNickname(),     // getName() 대신 getNickname() 또는 getUsername()
                product.getCategory().getCategoryId(),
                product.getCategory().getCategoryName(), // getName() 대신 getCategoryName()
                product.getName(),
                product.getOrigin(),
                product.getStorageType(),
                product.getWeight(),
                product.getWeightUnit(),
                product.isMandatoryAuction(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getAuctionDeadline(),
                product.getDescription(),
                product.getStatus(),
                images,
                tags,
                product.getCreatedAt()
        );
    }
}