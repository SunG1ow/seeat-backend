package com.seeat.seeatapi.domain.payment.repository;

import com.seeat.seeatapi.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderOrderId(Long orderId);
    boolean existsByPgTransactionId(String pgTransactionId); // 409 DUPLICATE_PG_TRANSACTION 검증
}