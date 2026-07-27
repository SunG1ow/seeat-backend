package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.global.response.PageResponse;

import java.math.BigDecimal;

// 7-1 상품 및 매출관리 대시보드
public record SellerDashboardResponse(
        PageResponse<SellerProductItem> products,
        SalesSummary salesSummary
) {
    public record SellerProductItem(
            Long productId,
            String name,
            int stockQuantity,
            String status
    ) {}

    public record SalesSummary(
            BigDecimal totalSalesAmount,
            long totalOrderCount
    ) {}
}