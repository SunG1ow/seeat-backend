package com.seeat.seeatapi.domain.order.repository;

import com.seeat.seeatapi.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByBuyerUserId(Long userId, Pageable pageable);
}