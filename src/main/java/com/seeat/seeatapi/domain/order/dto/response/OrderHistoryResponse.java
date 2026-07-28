package com.seeat.seeatapi.domain.order.dto.response;

import java.time.LocalDateTime;

// 4-5 사용자 구매 내역 조회 (목록 항목 하나, PageResponse<OrderHistoryResponse>로 감싸서 사용)
public record OrderHistoryResponse(
        Long orderId,
        String productName,
        String status,
        LocalDateTime orderedAt
) {}