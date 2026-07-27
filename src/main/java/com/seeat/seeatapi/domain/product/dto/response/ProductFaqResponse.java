package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.domain.product.entity.ProductFaq;

import java.time.LocalDateTime;

// 2-4 상품 문의 조회 응답
public record ProductFaqResponse(
        Long faqId,
        Long userId,
        String content,
        LocalDateTime createdAt
) {
    public static ProductFaqResponse from(ProductFaq faq) {
        return new ProductFaqResponse(
                faq.getFaqId(),
                faq.getMember().getUserId(),
                faq.getContent(),
                faq.getCreatedAt()
        );
    }
}