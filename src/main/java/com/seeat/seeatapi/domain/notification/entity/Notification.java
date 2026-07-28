package com.seeat.seeatapi.domain.notification.entity;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.order.entity.Order;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    // order_id는 nullable (5.3 스키마 기준)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    // changed_at과 동일 패턴: BaseEntity 미상속, sent_at에 직접 @CreatedDate
    @CreatedDate
    @Column(name = "sent_at", updatable = false, nullable = false)
    private LocalDateTime sentAt;

    protected Notification() {
    }

    // 4-2 결제완료(ORDER_CONFIRMED) / 4-3 상태변경(SHIPPING_STARTED, DELIVERY_COMPLETED) 시 트리거 생성
    public Notification(Member member, Order order, NotificationType type, String message) {
        this.member = member;
        this.order = order;
        this.type = type;
        this.message = message;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public Member getMember() {
        return member;
    }

    public Order getOrder() {
        return order;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}