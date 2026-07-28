package com.seeat.seeatapi.domain.order.repository;

import com.seeat.seeatapi.domain.order.entity.Order;
import com.seeat.seeatapi.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByBuyerUserId(Long userId, Pageable pageable);
    List<Order> findByOrderStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime deadline);
}