package com.seeat.seeatapi.domain.product.dto.request;

import com.seeat.seeatapi.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;

// [신규] 판매 상태 변경 요청
public record ProductStatusUpdateRequest(
        @NotNull ProductStatus status
) {}