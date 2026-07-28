package com.seeat.seeatapi.domain.delivery.dto.response;

// 4-6 주문 내역 및 배송 추적 (목록 항목 하나, tracking_url 없음 - v2.0 확정)
public record DeliveryTrackingResponse(
        Long orderId,
        String productName,
        String carrier,
        String trackingNumber,
        String status
) {}