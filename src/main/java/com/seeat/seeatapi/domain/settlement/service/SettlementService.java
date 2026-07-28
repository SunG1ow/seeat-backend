package com.seeat.seeatapi.domain.settlement.service;

import com.seeat.seeatapi.domain.settlement.dto.response.SettlementResponse;
import com.seeat.seeatapi.domain.settlement.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementRepository settlementRepository;

    public SettlementService(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    // 7-2 정산 내역 조회
    public List<SettlementResponse> getMySettlements(Long sellerId, String status) {
        return settlementRepository.findBySellerUserId(sellerId).stream()
                .filter(s -> status == null || s.getStatus().name().equals(status))
                .map(s -> new SettlementResponse(
                        s.getSettlementId(),
                        s.getOrder().getOrderId(),
                        s.getAmount(),
                        s.getStatus().name(),
                        s.getSettledAt()
                ))
                .collect(Collectors.toList());
    }
}