package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.domain.product.entity.ProductStatus;

// [신규] 판매 상태 변경 응답
public record ProductStatusResponse(
        Long productId,
        ProductStatus status
) {}