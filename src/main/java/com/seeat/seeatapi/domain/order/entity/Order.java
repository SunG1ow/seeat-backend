package com.seeat.seeatapi.domain.order.entity;

import com.seeat.seeatapi.domain.member.entity.DeliveryAddress;
import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.global.common.BaseEntity;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private DeliveryAddress deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "request_message", length = 255)
    private String requestMessage;

    // 4-8 관리자 취소가 SHIPPING 이전까지만 가능하다는 v2.1 정책 반영
    private static final Set<OrderStatus> ADMIN_CANCELLABLE_STATUSES =
            EnumSet.of(OrderStatus.PAYMENT_COMPLETED, OrderStatus.PREPARING);

    protected Order() {
    }

    // 4-1 주문서 작성
    public Order(Member buyer, DeliveryAddress deliveryAddress, BigDecimal totalAmount, String requestMessage) {
        this.buyer = buyer;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount = totalAmount;
        this.requestMessage = requestMessage;
        this.orderStatus = OrderStatus.PAYMENT_PENDING;
    }

    // 4-2 결제 완료
    public void completePayment() {
        if (this.orderStatus != OrderStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "이미 결제 완료된 주문입니다.");
        }
        this.orderStatus = OrderStatus.PAYMENT_COMPLETED;
    }

    // 4-3 관리자 배송 상태 변경 (PREPARING/SHIPPING/DELIVERED/PURCHASE_CONFIRMED)
    public void changeStatus(OrderStatus newStatus) {
        if (this.orderStatus == OrderStatus.PAYMENT_PENDING || this.orderStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "결제 미완료 또는 취소된 주문은 배송 상태를 변경할 수 없습니다.");
        }
        this.orderStatus = newStatus;
    }

    // 4-7 구매자 주문 취소: 결제 전에만 가능
    public void cancelByBuyer() {
        if (this.orderStatus != OrderStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "이미 결제 완료된 주문(결제 전 상태가 아님)");
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }

    // 결제 세션(10분) 만료로 인한 시스템 자동 취소
    public void expireByTimeout() {
        if (this.orderStatus != OrderStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "이미 결제 완료되었거나 처리된 주문입니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }

    // 4-8 관리자 취소(환불): 결제완료~준비중까지만 가능
    public void cancelByAdmin() {
        if (!ADMIN_CANCELLABLE_STATUSES.contains(this.orderStatus)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "이미 배송 시작(SHIPPING 이후)된 주문은 취소 불가");
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public boolean isOwnedBy(Long memberId) {
        return java.util.Objects.equals(this.buyer.getUserId(), memberId);
    }

    public Long getOrderId() {
        return orderId;
    }

    public Member getBuyer() {
        return buyer;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getRequestMessage() {
        return requestMessage;
    }
}