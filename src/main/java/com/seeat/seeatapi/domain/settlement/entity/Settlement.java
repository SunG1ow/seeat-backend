package com.seeat.seeatapi.domain.settlement.entity;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.order.entity.Order;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long settlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    // V3에서 order_id에 UNIQUE 추가 → 1:1 매핑 (payment/delivery와 동일 패턴)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    protected Settlement() {
    }

    // 즉시 입금 방식: 결제완료(4-2) 시점에 PENDING 상태로 정산 레코드 생성
    public Settlement(Member seller, Order order, BigDecimal amount) {
        this.seller = seller;
        this.order = order;
        this.amount = amount;
        this.status = SettlementStatus.PENDING;
    }

    // 7-2 정산 완료 처리
    public void complete() {
        if (this.status == SettlementStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "이미 정산 완료된 건입니다.");
        }
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = LocalDateTime.now();
    }

    public Long getSettlementId() {
        return settlementId;
    }

    public Member getSeller() {
        return seller;
    }

    public Order getOrder() {
        return order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }
}