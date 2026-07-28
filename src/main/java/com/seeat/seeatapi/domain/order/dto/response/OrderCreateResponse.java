package com.seeat.seeatapi.domain.order.dto.response;

import com.seeat.seeatapi.domain.order.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 4-1 주문서 작성 응답
public record OrderCreateResponse(
        Long orderId,
        String orderStatus,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getOrderId(),
                order.getOrderStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
}