package com.seeat.seeatapi.domain.notification.controller;

import com.seeat.seeatapi.domain.notification.dto.response.NotificationResponse;
import com.seeat.seeatapi.domain.notification.service.NotificationService;
import com.seeat.seeatapi.global.response.PageResponse;
import com.seeat.seeatapi.global.security.CurrentMemberId;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 5-3 알림 목록 조회
    @GetMapping
    public PageResponse<NotificationResponse> getNotifications(
            @CurrentMemberId Long memberId,
            Pageable pageable
    ) {
        return PageResponse.of(notificationService.getNotifications(memberId, pageable));
    }
}