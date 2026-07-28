package com.seeat.seeatapi.domain.notification.service;

import com.seeat.seeatapi.domain.notification.dto.response.NotificationResponse;
import com.seeat.seeatapi.domain.notification.entity.Notification;
import com.seeat.seeatapi.domain.notification.entity.NotificationType;
import com.seeat.seeatapi.domain.notification.repository.NotificationRepository;
import com.seeat.seeatapi.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // 4-2 결제완료 트리거
    @Transactional
    public void notifyOrderConfirmed(Order order) {
        String message = String.format("주문 결제가 완료되었습니다. (주문번호: %d)", order.getOrderId());
        save(order, NotificationType.ORDER_CONFIRMED, message);
    }

    // 4-3 배송시작 트리거
    @Transactional
    public void notifyShippingStarted(Order order) {
        String message = "상품 배송이 시작되었습니다.";
        save(order, NotificationType.SHIPPING_STARTED, message);
    }

    // 4-3 배송완료 트리거
    @Transactional
    public void notifyDeliveryCompleted(Order order) {
        String message = "상품 배송이 완료되었습니다.";
        save(order, NotificationType.DELIVERY_COMPLETED, message);
    }

    // 5-3 알림 목록 조회
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByMemberUserId(userId, pageable)
                .map(n -> new NotificationResponse(
                        n.getNotificationId(),
                        n.getType().name(),
                        n.getOrder() != null ? n.getOrder().getOrderId() : null,
                        n.getMessage(),
                        n.isRead(),
                        n.getSentAt()
                ));
    }

    private void save(Order order, NotificationType type, String message) {
        Notification notification = new Notification(order.getBuyer(), order, type, message);
        notificationRepository.save(notification);
    }
}