package com.seeat.seeatapi.domain.settlement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 7-2 정산 내역 조회 (목록 항목 하나)
public record SettlementResponse(
        Long settlementId,
        Long orderId,
        BigDecimal amount,
        String status,
        LocalDateTime settledAt
) {}