package com.seeat.seeatapi.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 2-1 수산물 상품 등록 (images는 Controller에서 별도 @RequestPart로 처리)
public record ProductCreateRequest(
        @NotNull Long categoryId,
        @NotBlank String name,
        @NotBlank String origin,
        @NotBlank String storageType,
        BigDecimal weight,
        String weightUnit,
        Boolean isMandatoryAuction,
        @NotNull @PositiveOrZero BigDecimal price,
        @NotNull @PositiveOrZero Integer stockQuantity,
        LocalDateTime auctionDeadline, // [신규] 위판 마감 시각
        String description,            // [신규] 상품 상세 설명
        List<String> tags
) {}