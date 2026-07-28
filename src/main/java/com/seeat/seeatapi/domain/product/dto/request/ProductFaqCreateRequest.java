package com.seeat.seeatapi.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 2-4 상품 문의 등록
public record ProductFaqCreateRequest(
        @NotBlank @Size(max = 500) String content
) {}