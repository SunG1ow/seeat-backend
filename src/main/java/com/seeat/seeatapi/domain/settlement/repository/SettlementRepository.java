package com.seeat.seeatapi.domain.settlement.repository;

import com.seeat.seeatapi.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findBySellerUserId(Long sellerId);
}