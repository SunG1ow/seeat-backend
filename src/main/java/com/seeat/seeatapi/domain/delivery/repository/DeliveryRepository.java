package com.seeat.seeatapi.domain.delivery.repository;

import com.seeat.seeatapi.domain.delivery.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderOrderId(Long orderId);

    @Query(
            "SELECT d FROM Delivery d WHERE d.order.buyer.userId = :userId " +
                    "AND (:startDate IS NULL OR d.order.createdAt >= :startDate) " +
                    "AND (:endDate IS NULL OR d.order.createdAt <= :endDate)"
    )
    Page<Delivery> findByBuyerAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}