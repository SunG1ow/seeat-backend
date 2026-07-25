package com.seeat.seeatapi.domain.delivery.repository;

import com.seeat.seeatapi.domain.delivery.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderOrderId(Long orderId);

    // 4-6 배송 추적 (기간 필터) - order 조인 필요하므로 JPQL 직접 작성
    @org.springframework.data.jpa.repository.Query(
            "SELECT d FROM Delivery d WHERE d.order.buyer.userId = :userId " +
                    "AND (:startDate IS NULL OR d.order.createdAt >= :startDate) " +
                    "AND (:endDate IS NULL OR d.order.createdAt <= :endDate)"
    )
    Page<Delivery> findByBuyerAndPeriod(
            @org.springframework.data.repository.query.Param("userId") Long userId,
            @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}