package com.seeat.seeatapi.domain.notification.dto.response;

import java.time.LocalDateTime;

// 5-3 알림 목록 조회 (목록 항목 하나)
public record NotificationResponse(
        Long notificationId,
        String type,
        Long orderId,
        String message,
        boolean isRead,
        LocalDateTime sentAt
) {}