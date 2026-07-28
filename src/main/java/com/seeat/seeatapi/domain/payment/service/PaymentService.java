package com.seeat.seeatapi.domain.payment.service;

import com.seeat.seeatapi.domain.order.entity.Order;
import com.seeat.seeatapi.domain.order.entity.OrderStatus;
import com.seeat.seeatapi.domain.order.entity.OrderStatusHistory;
import com.seeat.seeatapi.domain.order.repository.OrderRepository;
import com.seeat.seeatapi.domain.order.repository.OrderStatusHistoryRepository;
import com.seeat.seeatapi.domain.order.dto.response.PaymentResponse;
import com.seeat.seeatapi.domain.payment.entity.Payment;
import com.seeat.seeatapi.domain.payment.entity.PaymentMethod;
import com.seeat.seeatapi.domain.payment.repository.PaymentRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    // 4-2 결제 시도/승인 (OrderService.completePayment와 역할 분담: 여기서 실제 Payment 엔티티 저장)
    @Transactional
    public com.seeat.seeatapi.domain.order.dto.response.PaymentResponse pay(Long orderId, String paymentMethodStr, String pgTransactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (paymentRepository.existsByPgTransactionId(pgTransactionId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_PG_TRANSACTION);
        }

        order.completePayment();
        orderStatusHistoryRepository.save(new OrderStatusHistory(order, OrderStatus.PAYMENT_COMPLETED));

        PaymentMethod method = PaymentMethod.valueOf(paymentMethodStr);
        Payment payment = new Payment(order, method, pgTransactionId);
        paymentRepository.save(payment);

        return new com.seeat.seeatapi.domain.order.dto.response.PaymentResponse(
                payment.getPaymentId(),
                order.getOrderId(),
                order.getOrderStatus().name(),
                payment.getPaymentMethod().name(),
                payment.getPgTransactionId()
        );
    }
}