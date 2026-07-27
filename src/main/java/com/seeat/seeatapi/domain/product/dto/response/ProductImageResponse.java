package com.seeat.seeatapi.domain.product.dto.response;

import java.util.List;

// 2-6 이미지 추가 응답
public record ProductImageResponse(
        List<String> imageUrls
) {}