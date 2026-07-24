package com.seeat.seeatapi.domain.payment.entity;

import com.seeat.seeatapi.domain.order.entity.Order;
import jakarta.persistence.*;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "pg_transaction_id", nullable = false, unique = true, length = 100)
    private String pgTransactionId;

    protected Payment() {
    }

    // 4-2 결제 시도/승인
    public Payment(Order order, PaymentMethod paymentMethod, String pgTransactionId) {
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.pgTransactionId = pgTransactionId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Order getOrder() {
        return order;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getPgTransactionId() {
        return pgTransactionId;
    }
}