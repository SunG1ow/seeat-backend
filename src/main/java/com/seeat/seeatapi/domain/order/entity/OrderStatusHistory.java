package com.seeat.seeatapi.domain.order.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@EntityListeners(AuditingEntityListener.class)
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_value", nullable = false, length = 30)
    private OrderStatus statusValue;

    @CreatedDate
    @Column(name = "changed_at", updatable = false, nullable = false)
    private LocalDateTime changedAt;

    protected OrderStatusHistory() {
    }

    // 주문 상태가 바뀔 때마다 이력 기록 (4-1~4-3, 4-7, 4-8 전 과정에서 호출)
    public OrderStatusHistory(Order order, OrderStatus statusValue) {
        this.order = order;
        this.statusValue = statusValue;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public Order getOrder() {
        return order;
    }

    public OrderStatus getStatusValue() {
        return statusValue;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}