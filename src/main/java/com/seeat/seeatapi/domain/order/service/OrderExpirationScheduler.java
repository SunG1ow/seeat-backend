package com.seeat.seeatapi.domain.order.service;

import com.seeat.seeatapi.domain.order.entity.Order;
import com.seeat.seeatapi.domain.order.entity.OrderStatus;
import com.seeat.seeatapi.domain.order.entity.OrderStatusHistory;
import com.seeat.seeatapi.domain.order.repository.OrderItemRepository;
import com.seeat.seeatapi.domain.order.repository.OrderRepository;
import com.seeat.seeatapi.domain.order.repository.OrderStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderExpirationScheduler.class);
    private static final int PAYMENT_SESSION_MINUTES = 10;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderExpirationScheduler(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    // 1분마다 실행: 결제 세션(10분) 만료된 PAYMENT_PENDING 주문을 자동 취소 + 재고 복원
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void expirePendingOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAYMENT_SESSION_MINUTES);

        List<Order> expiredOrders = orderRepository
                .findByOrderStatusAndCreatedAtBefore(OrderStatus.PAYMENT_PENDING, deadline);

        for (Order order : expiredOrders) {
            order.expireByTimeout();
            orderStatusHistoryRepository.save(new OrderStatusHistory(order, OrderStatus.CANCELLED));

            orderItemRepository.findByOrderOrderId(order.getOrderId())
                    .forEach(item -> item.getProduct().increaseStock(item.getQuantity()));

            log.info("결제 세션 만료로 주문 자동 취소 - orderId: {}", order.getOrderId());
        }
    }
}