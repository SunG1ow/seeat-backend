package com.seeat.seeatapi.domain.order.repository;

import com.seeat.seeatapi.domain.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderOrderIdOrderByChangedAtAsc(Long orderId); // 4-4 이력 조회
}