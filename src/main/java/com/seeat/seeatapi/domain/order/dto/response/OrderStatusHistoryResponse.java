package com.seeat.seeatapi.domain.order.dto.response;

import com.seeat.seeatapi.domain.order.entity.OrderStatusHistory;

import java.time.LocalDateTime;

// 4-4 주문 상태 이력 조회 (목록 항목 하나)
public record OrderStatusHistoryResponse(
        Long historyId,
        String statusValue,
        LocalDateTime changedAt
) {
    public static OrderStatusHistoryResponse from(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getHistoryId(),
                history.getStatusValue().name(),
                history.getChangedAt()
        );
    }
}