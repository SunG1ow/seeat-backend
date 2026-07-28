package com.seeat.seeatapi.domain.order.dto.response;

import java.time.LocalDateTime;

// 4-3 상태 변경 응답
public record OrderStatusChangeResponse(
        Long orderId,
        String status,
        LocalDateTime notifiedAt,
        String notificationChannel
) {}