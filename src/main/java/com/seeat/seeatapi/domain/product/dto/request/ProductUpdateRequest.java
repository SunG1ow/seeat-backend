package com.seeat.seeatapi.domain.product.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 2-5 상품 정보 수정 (전 필드 선택, 부분 수정 지원)
public record ProductUpdateRequest(
        String name,
        String origin,
        String storageType,
        BigDecimal weight,
        String weightUnit,
        BigDecimal price,
        Integer stockQuantity,
        LocalDateTime auctionDeadline, // [신규] 위판 마감 시각
        String description,            // [신규] 상품 상세 설명
        List<String> tags
) {}